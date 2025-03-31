package horizon.core.util;

import horizon.core.annotation.Sentinel;
import horizon.core.model.Raw;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.pipeline.SentinelInterface;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SentinelScanner {

    public static <T extends RawInput> List<SentinelInterface.InboundSentinel<T>> scanInbound(Set<RawInput.Scheme> supportedSchemes) {
        return specialize(scanSentinels(
                supportedSchemes,
                Sentinel.SentinelDirection.INBOUND,
                SentinelInterface.InboundSentinel.class
        ));
    }

    public static <T extends RawOutput> List<SentinelInterface.OutboundSentinel<T>> scanOutbound(Set<RawInput.Scheme> supportedSchemes) {
        return specialize(scanSentinels(
                supportedSchemes,
                Sentinel.SentinelDirection.OUTBOUND,
                SentinelInterface.OutboundSentinel.class
        ));
    }

    private static <S extends SentinelInterface, T extends Raw> List<S> scanSentinels(Set<RawInput.Scheme> schemes,
                                                                                      Sentinel.SentinelDirection directionValue,
                                                                                      Class<S> targetType) {

        String basePackage = BasePackageScanner.findBasePackage();
        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Sentinel.class);
        List<S> result = new ArrayList<>();

        for (Class<?> clazz : annotatedClasses) {
            Sentinel meta = clazz.getAnnotation(Sentinel.class);
            if (meta.direction() != directionValue && meta.direction() != Sentinel.SentinelDirection.BOTH) {
                continue;
            }

            boolean schemeMatched = Arrays.stream(meta.scheme()).anyMatch(schemes::contains);
            if (!schemeMatched) continue;

            if (targetType.isAssignableFrom(clazz)) {
                try {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    result.add(targetType.cast(instance));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to create Sentinel instance: " + clazz.getName(), e);
                }
            }
        }

        // Order 기반 정렬
        return result.stream()
                .sorted(Comparator.comparingInt(c -> c.getClass().getAnnotation(Sentinel.class).order()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static <S> List<S> specialize(List<?> input) {
        return (List<S>) input;
    }

}