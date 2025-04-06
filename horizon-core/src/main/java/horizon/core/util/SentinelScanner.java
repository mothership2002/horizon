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
     * Automatically creates a new SentinelScanner instance using the application's root package.
     *
     * <p>This method detects the application root package via {@link #detectApplicationRootPackage()},
     * logs the detected package, and initializes a new SentinelScanner with it.</p>
     *
     * @return a new SentinelScanner instance configured with the detected root package.
     */
    public static SentinelScanner auto() {
        String basePackage = detectApplicationRootPackage();
        log.info("[Horizon] Detected application root package: {}", basePackage);
        return new SentinelScanner(basePackage);
    }

    /**
     * Detects the application's root package based on a class annotated with {@code @HorizonApplication}.
     *
     * <p>This method scans all packages using the Reflections library to locate classes annotated with
     * {@code @HorizonApplication}. It returns the package name of the first matching class. If no such class is found,
     * an {@link IllegalStateException} is thrown, and any other errors during the scanning process result in a
     * {@link RuntimeException} wrapping the underlying exception.</p>
     *
     * @return the package name of the first class annotated with {@code @HorizonApplication}
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
     * Constructs a new SentinelScanner and initiates scanning for sentinel classes
     * within the specified base packages.
     *
     * <p>This constructor logs the start of the scan, scans the provided packages for classes
     * annotated with {@code @Sentinel}, and logs a summary of the scanning results.</p>
     *
     * @param basePackages the base packages to scan for sentinel classes
     */
    public SentinelScanner(String... basePackages) {
        log.info("Starting Sentinel scan...");
        scanPackages(basePackages);
        logSummary();
    }

    /**
     * Scans the specified packages for classes annotated with {@code Sentinel} and registers them as inbound
     * and/or outbound sentinels according to their configuration.
     *
     * <p>This method employs the Reflections library to locate classes marked with the {@code Sentinel} annotation,
     * filters out those that do not extend {@link FlowSentinel}, and instantiates each valid class using its no-argument
     * constructor. For every scheme declared in the annotation, it registers the sentinel as inbound and/or outbound based
     * on its specified direction.
     *
     * @param basePackages the array of package names to scan for {@code Sentinel}-annotated classes
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
     * Registers an inbound sentinel for the specified scheme.
     *
     * <p>Adds the provided inbound sentinel to the list associated with the given scheme.
     * If no list exists for the scheme, a new one is created. The method then sorts the list
     * based on the order defined by the sentinel's annotation, as determined by the {@code getOrder} method.</p>
     *
     * @param scheme the scheme identifier for grouping inbound sentinels
     * @param sentinel the inbound sentinel instance to register
     */
    private void registerInbound(String scheme, FlowSentinel.InboundSentinel<?> sentinel) {
        inboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        inboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Registers an outbound sentinel under the specified scheme.
     *
     * <p>If a list for the given scheme does not exist, one is created. The provided sentinel is added to this list,
     * which is then sorted based on the order obtained from the sentinel's annotation.</p>
     *
     * @param scheme the identifier for grouping outbound sentinels
     * @param sentinel the outbound sentinel instance to register
     */
    private void registerOutbound(String scheme, FlowSentinel.OutboundSentinel<?> sentinel) {
        outboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        outboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Retrieves the order value from the sentinel's {@code @Sentinel} annotation.
     *
     * <p>If the sentinel's class is annotated with {@code @Sentinel}, its defined order is returned.
     * Otherwise, returns {@code Integer.MAX_VALUE} to indicate a default lowest priority.</p>
     *
     * @param sentinel the sentinel instance to inspect for an {@code @Sentinel} annotation
     * @return the order value from the annotation, or {@code Integer.MAX_VALUE} if the annotation is not present
     */
    private int getOrder(Object sentinel) {
        Sentinel annotation = sentinel.getClass().getAnnotation(Sentinel.class);
        return annotation != null ? annotation.order() : 2_147_483_647;
    }

    /**
     * Retrieves the list of inbound sentinels registered under the specified scheme.
     *
     * <p>If no inbound sentinels are associated with the scheme, an empty list is returned.
     *
     * @param scheme the scheme for which to retrieve inbound sentinels
     * @return the list of inbound sentinels or an empty list if none are registered
     */
    public List<FlowSentinel.InboundSentinel<? extends RawInput>> getInboundSentinels(String scheme) {
        return inboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Returns the list of outbound sentinels registered for the given scheme.
     *
     * If no sentinels are registered under the specified scheme, this method returns an empty list.
     *
     * @param scheme the identifier of the scheme for which outbound sentinels are retrieved
     * @return a list of outbound sentinels associated with the scheme, or an empty list if none are registered
     */
    public List<FlowSentinel.OutboundSentinel<? extends RawOutput>> getOutboundSentinels(String scheme) {
        return outboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Logs a summary of registered inbound and outbound sentinels.
     * <p>
     * Iterates through the inbound and outbound sentinel maps, outputting debug-level
     * information for each associated scheme and the simple class name of each sentinel.
     * </p>
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