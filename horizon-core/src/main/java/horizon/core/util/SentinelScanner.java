package horizon.core.util;

import horizon.core.annotation.HorizonApplication;
import horizon.core.annotation.Sentinel;
import horizon.core.constant.Scheme;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SentinelScanner {

    private static final Logger log = LoggerFactory.getLogger(SentinelScanner.class);

    private final Map<String, List<FlowSentinel.InboundSentinel<? extends RawInput>>> inboundSentinels = new HashMap<>();
    private final Map<String, List<FlowSentinel.OutboundSentinel<? extends RawOutput>>> outboundSentinels = new HashMap<>();

    /**
     * Automatically creates a new SentinelScanner instance using the detected application root package.
     *
     * <p>This method locates the class annotated with {@code @HorizonApplication} to determine the base package,
     * logs the detected package, and initializes a SentinelScanner instance with it.</p>
     *
     * @return a SentinelScanner instance configured with the application's root package
     */
    public static SentinelScanner auto() {
        String basePackage = detectApplicationRootPackage();
        log.info("[Horizon] Detected application root package: {}", basePackage);
        return new SentinelScanner(basePackage);
    }

    /**
     * Detects and returns the application's root package by scanning for classes annotated with {@code @HorizonApplication}.
     *
     * <p>This method scans the classpath using the Reflections library and returns the package name of the first class
     * found annotated with {@code @HorizonApplication}. If no such class is detected, it throws an {@link IllegalStateException}.
     * Any other exceptions encountered during scanning are wrapped in a {@link RuntimeException}.</p>
     *
     * @return the package name of the detected application class
     * @throws IllegalStateException if no class annotated with {@code @HorizonApplication} is found
     * @throws RuntimeException if an error occurs during the scanning process
     */
    private static String detectApplicationRootPackage() {
        try {
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .forPackages("") // 전체 스캔
                            .addScanners(Scanners.TypesAnnotated)
            );

            Set<Class<?>> apps = reflections.getTypesAnnotatedWith(HorizonApplication.class);

            if (apps.isEmpty()) {
                throw new IllegalStateException("No @HorizonApplication class found.");
            }

            Class<?> appClass = apps.iterator().next();
            return appClass.getPackage().getName();

        } catch (Exception e) {
            throw new RuntimeException("Failed to detect @HorizonApplication base package", e);
        }
    }

    /**
     * Constructs a new SentinelScanner that scans the specified base packages for classes annotated with {@code @Sentinel}
     * and registers them as inbound or outbound sentinels.
     *
     * <p>This constructor logs the start of the scanning process, invokes the package scanning to locate Sentinel classes, 
     * and logs a summary of the registered sentinels.
     *
     * @param basePackages one or more package names to scan for Sentinel classes.
     */
    public SentinelScanner(String... basePackages) {
        log.info("Starting Sentinel scan...");
        scanPackages(basePackages);
        logSummary();
    }

    /**
     * Scans the specified base packages for classes annotated with {@code @Sentinel} and registers them
     * as inbound and/or outbound sentinels based on the annotation's declared schemes and directions.
     *
     * <p>This method leverages the Reflections library to find classes that extend {@link FlowSentinel} and are
     * annotated with {@code @Sentinel}. It dynamically instantiates each matching class using its no-argument
     * constructor and, for each scheme declared in the annotation, registers the sentinel as inbound if its
     * direction is {@code INBOUND} or {@code BOTH}, and as outbound if its direction is {@code OUTBOUND} or
     * {@code BOTH}.</p>
     *
     * @param basePackages the array of package names to scan for sentinel classes
     * @throws RuntimeException if instantiation of a sentinel fails
     */
    private void scanPackages(String[] basePackages) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(basePackages)
                        .addScanners(Scanners.TypesAnnotated)
        );

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Sentinel.class);

        for (Class<?> clazz : annotated) {
            if (!FlowSentinel.class.isAssignableFrom(clazz)) continue;

            Sentinel meta = clazz.getAnnotation(Sentinel.class);
            Object instance;

            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate sentinel: " + clazz.getName(), e);
            }

            for (Scheme scheme : meta.scheme()) {
                String schemeKey = scheme.name();
                Sentinel.SentinelDirection direction = meta.direction();

                if (direction == Sentinel.SentinelDirection.INBOUND || direction == Sentinel.SentinelDirection.BOTH) {
                    registerInbound(schemeKey, (FlowSentinel.InboundSentinel<?>) instance);
                }
                if (direction == Sentinel.SentinelDirection.OUTBOUND || direction == Sentinel.SentinelDirection.BOTH) {
                    registerOutbound(schemeKey, (FlowSentinel.OutboundSentinel<?>) instance);
                }
            }
        }
    }

    /**
     * Registers an inbound sentinel under the specified scheme.
     *
     * <p>This method adds the provided inbound sentinel to the list associated with the given scheme,
     * creating a new list if one does not exist. The sentinels are subsequently sorted based on the order
     * value extracted from their annotation.</p>
     *
     * @param scheme the identifier for the scheme under which the sentinel is registered
     * @param sentinel the inbound sentinel to register
     */
    private void registerInbound(String scheme, FlowSentinel.InboundSentinel<?> sentinel) {
        inboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        inboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Registers an outbound sentinel for the given scheme and sorts the sentinels by their order.
     *
     * <p>This method adds the specified outbound sentinel to the list associated with the provided scheme.
     * After insertion, the list is sorted based on the order value determined by the sentinel's annotation.
     *
     * @param scheme the unique identifier for the outbound sentinel category
     * @param sentinel the outbound sentinel instance to register
     */
    private void registerOutbound(String scheme, FlowSentinel.OutboundSentinel<?> sentinel) {
        outboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        outboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Retrieves the order value from the sentinel's @Sentinel annotation.
     *
     * <p>If the sentinel's class is not annotated with @Sentinel, the method returns Integer.MAX_VALUE (2_147_483_647).</p>
     *
     * @param sentinel the object for which to determine the order value
     * @return the order value specified in the @Sentinel annotation, or Integer.MAX_VALUE if not present
     */
    private int getOrder(Object sentinel) {
        Sentinel annotation = sentinel.getClass().getAnnotation(Sentinel.class);
        return annotation != null ? annotation.order() : 2_147_483_647;
    }

    /**
     * Returns a list of inbound sentinels associated with the specified scheme.
     *
     * <p>If no inbound sentinels are registered under the given scheme, an empty list is returned.</p>
     *
     * @param scheme the scheme identifier used for retrieving inbound sentinels
     * @return a list of inbound sentinels for the specified scheme, or an empty list if none are registered
     */
    public List<FlowSentinel.InboundSentinel<? extends RawInput>> getInboundSentinels(String scheme) {
        return inboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Retrieves the outbound sentinels associated with the given scheme.
     *
     * <p>If no sentinels are registered for the specified scheme, an empty list is returned.
     *
     * @param scheme the scheme identifier to look up associated outbound sentinels
     * @return a list of outbound sentinels linked to the specified scheme, or an empty list if none exist
     */
    public List<FlowSentinel.OutboundSentinel<? extends RawOutput>> getOutboundSentinels(String scheme) {
        return outboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Logs a summary of registered inbound and outbound sentinels.
     *
     * <p>This method iterates over the inbound and outbound sentinel maps, outputting a debug-level log entry for each scheme
     * along with the simple class names of the associated sentinel instances.</p>
     */
    private void logSummary() {
        log.debug("== Sentinel Scanning Result ==");
        inboundSentinels.forEach((scheme, list) -> {
            log.debug("[INBOUND - {}]", scheme);
            list.forEach(s -> log.debug(" ↳ {}", s.getClass().getSimpleName()));
        });

        outboundSentinels.forEach((scheme, list) -> {
            log.debug("[OUTBOUND - {}]", scheme);
            list.forEach(s -> log.debug(" ↳ {}", s.getClass().getSimpleName()));
        });
    }
}