package horizon.core.parameter;

import horizon.core.annotation.Header;
import horizon.core.annotation.PathParam;
import horizon.core.annotation.QueryParam;
import horizon.core.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;

public class ParameterHelper {

    private final Map<Class<? extends Annotation>, ParameterAnalyzer> analyzers;

    public ParameterHelper() {
        this.analyzers = Map.of(
                PathParam.class, (parameter, index) -> {
                    PathParam ann = parameter.getAnnotation(PathParam.class);
                    return ParameterInfo.builder()
                            .parameter(parameter)
                            .type(parameter.getType())
                            .index(index)
                            .source(ParameterSource.PATH)
                            .name(Objects.requireNonNull(ann).value())
                            .build();
                },
                QueryParam.class, (parameter, index) -> {
                    QueryParam ann = parameter.getAnnotation(QueryParam.class);
                    return ParameterInfo.builder()
                            .parameter(parameter)
                            .type(parameter.getType())
                            .index(index)
                            .source(ParameterSource.QUERY)
                            .name(Objects.requireNonNull(ann).value())
                            .required(ann.required())
                            .defaultValue(ann.defaultValue().isEmpty() ? null : ann.defaultValue())
                            .build();
                },
                Header.class, (parameter, index) -> {
                    Header ann = parameter.getAnnotation(Header.class);
                    return ParameterInfo.builder()
                            .parameter(parameter)
                            .type(parameter.getType())
                            .index(index)
                            .source(ParameterSource.HEADER)
                            .name(Objects.requireNonNull(ann).value())
                            .required(ann.required())
                            .build();
                },
                RequestBody.class, (parameter, index) -> {
                    RequestBody ann = parameter.getAnnotation(RequestBody.class);
                    return ParameterInfo.builder()
                            .parameter(parameter)
                            .type(parameter.getType())
                            .index(index)
                            .source(ParameterSource.BODY)
                            .name("body")
                            .required(Objects.requireNonNull(ann).required())
                            .build();
                }
        );
    }

    public ParameterInfo analyze(Parameter parameter, int index) {
        for (Class<? extends Annotation> annotationType : analyzers.keySet()) {
            if (parameter.isAnnotationPresent(annotationType)) {
                return analyzers.get(annotationType).analyze(parameter, index);
            }
        }

        // No annotation - for backward compatibility, non-primitive types are assumed to be body
        if (!isPrimitiveOrWrapper(parameter.getType()) 
                && !parameter.getType().equals(String.class)) {
            return ParameterInfo.builder()
                    .parameter(parameter)
                    .type(parameter.getType())
                    .index(index)
                    .source(ParameterSource.BODY)
                    .name("body")
                    .required(false) // Default to isn't require for an implicit body
                    .build();
        } else {
            // Primitive types without annotation are not allowed
            throw new IllegalArgumentException(
                String.format("Parameter '%s' must have an annotation (@PathParam, @QueryParam, @Header, or @RequestBody)",
                    parameter.getName())
            );
        }
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || 
               type == Boolean.class || type == Byte.class || 
               type == Character.class || type == Short.class || 
               type == Integer.class || type == Long.class || 
               type == Float.class || type == Double.class;
    }

    @FunctionalInterface
    private interface ParameterAnalyzer {
        ParameterInfo analyze(Parameter parameter, int index);
    }
}