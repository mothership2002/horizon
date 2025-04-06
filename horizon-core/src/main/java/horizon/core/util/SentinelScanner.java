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

    public static SentinelScanner auto() {
        String basePackage = detectApplicationRootPackage();
        log.info("[Horizon] Detected application root package: {}", basePackage);
        return new SentinelScanner(basePackage);
    }

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

    public SentinelScanner(String... basePackages) {
        log.info("Starting Sentinel scan...");
        scanPackages(basePackages);
        logSummary();
    }

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

    private void registerInbound(String scheme, FlowSentinel.InboundSentinel<?> sentinel) {
        inboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        inboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    private void registerOutbound(String scheme, FlowSentinel.OutboundSentinel<?> sentinel) {
        outboundSentinels.computeIfAbsent(scheme, k -> new ArrayList<>()).add(sentinel);
        outboundSentinels.get(scheme).sort(Comparator.comparingInt(this::getOrder));
    }

    private int getOrder(Object sentinel) {
        Sentinel annotation = sentinel.getClass().getAnnotation(Sentinel.class);
        return annotation != null ? annotation.order() : 2_147_483_647;
    }

    public List<FlowSentinel.InboundSentinel<? extends RawInput>> getInboundSentinels(String scheme) {
        return inboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

    public List<FlowSentinel.OutboundSentinel<? extends RawOutput>> getOutboundSentinels(String scheme) {
        return outboundSentinels.getOrDefault(scheme, Collections.emptyList());
    }

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