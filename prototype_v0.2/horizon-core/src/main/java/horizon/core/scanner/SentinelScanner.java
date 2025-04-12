package horizon.core.scanner;

import horizon.core.annotation.HorizonApplication;
import horizon.core.annotation.Sentinel;
import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.sentinel.InboundSentinel;
import horizon.core.rendezvous.sentinel.OutboundSentinel;
import horizon.core.util.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings("unchecked")
public class SentinelScanner<I extends RawInput, O extends RawOutput> {

    private static final Logger log = LoggerFactory.getLogger(SentinelScanner.class);

    private final Map<String, List<InboundSentinel<I>>> inboundSentinels = new HashMap<>();
    private final Map<String, List<OutboundSentinel<O>>> outboundSentinels = new HashMap<>();

    /**
     * Creates and returns a SentinelScanner instance based on the detected application root package.
     *
     * <p>This method uses {@link #detectApplicationRootPackage()} to determine the application's root package 
     * (the package containing the class annotated with {@code @HorizonApplication}). It logs the detected package 
     * and initializes a new SentinelScanner with that package.</p>
     *
     * @return a SentinelScanner initialized with the detected application root package
     */
    public static <I extends RawInput, O extends RawOutput> SentinelScanner<I, O> auto() {
        String basePackage = detectApplicationRootPackage();
        log.info("[Horizon] Detected application root package: {}", basePackage);
        return new SentinelScanner<>(basePackage);
    }

    /**
     * Detects the application's root package by scanning for a class annotated with {@code @HorizonApplication}.
     *
     * <p>This method employs the Reflections library to scan all packages for classes with the
     * {@code @HorizonApplication} annotation and returns the package name of the first discovered class.
     * If no such class is found, an {@link IllegalStateException} is thrown. Any exceptions encountered during
     * scanning are rethrown as a {@link RuntimeException}.</p>
     *
     * @return the package name of the first {@code @HorizonApplication}-annotated class
     * @throws IllegalStateException if no class annotated with {@code @HorizonApplication} is found
     * @throws RuntimeException if an error occurs during scanning
     */
    private static String detectApplicationRootPackage() {
        try {
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .forPackages("")
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
     * Initializes a new SentinelScanner and scans the specified base packages for sentinel classes.
     *
     * <p>This constructor logs the initiation of the scan, processes the provided base packages to discover classes
     * annotated with {@code @Sentinel}, and logs a summary of the registered inbound and outbound sentinels.</p>
     *
     * @param basePackages one or more base package names to search for sentinel classes
     */
    public SentinelScanner(String... basePackages) {
        log.info("Starting Sentinel scan...");
        scanPackages(basePackages);
        logSummary();
    }

    /**
     * Scans the specified packages for classes annotated with {@code @Sentinel} and registers each valid sentinel
     * as an inbound and/or outbound sentinel based on its configured direction.
     *
     * <p>This method uses the Reflections library to locate classes within the provided base packages. For each
     * class that is a subtype of {@code FlowSentinel}, it instantiates the class via a no-argument constructor and,
     * for every scheme defined in its {@code @Sentinel} annotation, registers the instance using {@code registerInbound()}
     * if the direction is INBOUND or BOTH, and using {@code registerOutbound()} if the direction is OUTBOUND or BOTH.
     * If instantiation of a sentinel fails, a {@code RuntimeException} is thrown.
     *
     * @param basePackages the array of package names to scan for sentinel classes
     */
    private void scanPackages(String[] basePackages) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(basePackages)
                        .addScanners(Scanners.TypesAnnotated)
        );

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Sentinel.class);

        for (Class<?> clazz : annotated) {
            if (isNeitherInboundNorOutbound(clazz)) continue;

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
                    registerInbound(schemeKey, (InboundSentinel<I>) instance);
                }
                if (direction == Sentinel.SentinelDirection.OUTBOUND || direction == Sentinel.SentinelDirection.BOTH) {
                    registerOutbound(schemeKey, (OutboundSentinel<O>) instance);
                }
            }
        }
    }

    public static boolean isNeitherInboundNorOutbound(Class<?> clazz) {
        return !ClassUtils.isAssignable(InboundSentinel.class, clazz)
                && !ClassUtils.isAssignable(OutboundSentinel.class, clazz);
    }

    /**
     * Registers an inbound sentinel under the specified scheme and sorts the associated list based on the sentinel order.
     *
     * <p>The inbound sentinel is added to the list of sentinels corresponding to the given scheme. If no list exists,
     * one is created. After registration, the list is re-sorted using the order returned by {@link #getOrder(Object)}
     * to ensure proper processing sequence.</p>
     *
     * @param scheme the scheme identifier used to group inbound sentinels
     * @param sentinel the inbound sentinel instance to be registered
     */
    private void registerInbound(String scheme, InboundSentinel<I> sentinel) {
        inboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        inboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Registers an outbound sentinel under the specified scheme.
     *
     * <p>This method adds the provided outbound sentinel to a list associated with the given scheme.
     * If no list exists for that scheme, one is created. The sentinel list is then sorted based on the
     * order defined in each sentinel's annotation.</p>
     *
     * @param scheme the name of the scheme under which the sentinel is registered
     * @param sentinel the outbound sentinel to register
     */
    private void registerOutbound(String scheme, OutboundSentinel<O> sentinel) {
        outboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        outboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    /**
     * Retrieves the order value specified by the {@code @Sentinel} annotation on the sentinel's class.
     * <p>
     * If the sentinel is not annotated with {@code @Sentinel}, {@code Integer.MAX_VALUE} is returned as the default order.
     *
     * @param sentinel the sentinel instance from which to extract the order value
     * @return the order defined in the {@code @Sentinel} annotation, or {@code Integer.MAX_VALUE} if the annotation is absent
     */
    private int getOrder(Object sentinel) {
        Sentinel annotation = sentinel.getClass().getAnnotation(Sentinel.class);
        return annotation != null ? annotation.order() : 2_147_483_647;
    }

    /**
     * Retrieves the list of inbound sentinels registered under the specified scheme.
     * <p>
     * If no inbound sentinels are associated with the scheme, this method returns an empty list.
     *
     * @param scheme the identifier for the scheme whose inbound sentinels are requested
     * @return a list of inbound sentinels for the given scheme, or an empty list if none exist
     */
    public List<InboundSentinel<I>> getInboundSentinels(String scheme) {
        return inboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Retrieves the list of outbound sentinels associated with the specified scheme.
     *
     * @param scheme the scheme identifier used to categorize outbound sentinels
     * @return a list of outbound sentinels registered under the specified scheme, or an empty list if none are registered
     */
    public List<OutboundSentinel<O>> getOutboundSentinels(String scheme) {
        return outboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    /**
     * Logs a debug-level summary of all registered inbound and outbound sentinels.
     *
     * <p>This method iterates over the inbound and outbound sentinel maps, printing the scheme
     * names and the simple class names of each sentinel associated with the respective scheme.
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