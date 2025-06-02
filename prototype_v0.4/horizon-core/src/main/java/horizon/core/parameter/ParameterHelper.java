package horizon.core.parameter;

import horizon.core.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class for analyzing method parameters and their annotations.
 * Supports protocol-neutral @Param annotation.
 */
public class ParameterHelper {
    private static final Logger logger = LoggerFactory.getLogger(ParameterHelper.class);

    public ParameterHelper() {
    }

    public ParameterInfo analyze(Parameter parameter, int index) {
        // 1. Check for @Param (protocol-neutral)
        if (parameter.isAnnotationPresent(Param.class)) {
            return analyzeParam(parameter, index);
        }

        // 2. No annotation - infer from type
        return inferParameter(parameter, index);
    }

    /**
     * Analyzes @Param annotation (protocol-neutral).
     */
    private ParameterInfo analyzeParam(Parameter parameter, int index) {
        Param ann = parameter.getAnnotation(Param.class);
        return ParameterInfo.builder()
            .parameter(parameter)
            .type(parameter.getType())
            .index(index)
            .source(ParameterSource.PARAM)
            .name(Objects.requireNonNull(ann).value())
            .required(ann.required())
            .defaultValue(ann.defaultValue().isEmpty() ? null : ann.defaultValue())
            .hints(ann.hints())
            .build();
    }


    /**
     * Infers parameter info when no annotation is present.
     * For backward compatibility with non-annotated parameters.
     */
    private ParameterInfo inferParameter(Parameter parameter, int index) {
        // Non-primitive types are assumed to be from body (legacy behavior)
        if (!isPrimitiveOrWrapper(parameter.getType()) 
                && !parameter.getType().equals(String.class)) {
            return ParameterInfo.builder()
                .parameter(parameter)
                .type(parameter.getType())
                .index(index)
                .source(ParameterSource.BODY)
                .name("body")
                .required(false)
                .hints(new String[]{"body"})
                .build();
        } else {
            // Primitive types without annotation - use AUTO mode
            return ParameterInfo.builder()
                .parameter(parameter)
                .type(parameter.getType())
                .index(index)
                .source(ParameterSource.AUTO)
                .name(parameter.getName())
                .required(false)
                .build();
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
