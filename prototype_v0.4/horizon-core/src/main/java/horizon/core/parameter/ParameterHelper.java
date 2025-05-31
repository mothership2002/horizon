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
 * Supports both protocol-neutral @Param and legacy HTTP-specific annotations.
 */
public class ParameterHelper {
    private static final Logger logger = LoggerFactory.getLogger(ParameterHelper.class);
    
    private final Map<Class<? extends Annotation>, ParameterAnalyzer> analyzers;
    private final Map<Class<? extends Annotation>, ParameterAnalyzer> legacyAnalyzers;

    public ParameterHelper() {
        // Protocol-neutral analyzer
        this.analyzers = Map.of(
            Param.class, this::analyzeParam
        );
        
        // Legacy HTTP-specific analyzers (deprecated)
        this.legacyAnalyzers = Map.of(
            PathParam.class, this::analyzePathParam,
            QueryParam.class, this::analyzeQueryParam,
            Header.class, this::analyzeHeader,
            RequestBody.class, this::analyzeRequestBody
        );
    }

    public ParameterInfo analyze(Parameter parameter, int index) {
        // 1. Check for @Param (protocol-neutral)
        if (parameter.isAnnotationPresent(Param.class)) {
            return analyzeParam(parameter, index);
        }
        
        // 2. Check for legacy annotations (deprecated)
        for (Class<? extends Annotation> annotationType : legacyAnalyzers.keySet()) {
            if (parameter.isAnnotationPresent(annotationType)) {
                logger.warn("@{} is deprecated. Use @Param instead.", 
                           annotationType.getSimpleName());
                return legacyAnalyzers.get(annotationType).analyze(parameter, index);
            }
        }

        // 3. No annotation - infer from type
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
            .name(ann.value())
            .required(ann.required())
            .defaultValue(ann.defaultValue().isEmpty() ? null : ann.defaultValue())
            .hints(ann.hints())
            .build();
    }
    
    /**
     * Legacy analyzers for backward compatibility.
     */
    private ParameterInfo analyzePathParam(Parameter parameter, int index) {
        PathParam ann = parameter.getAnnotation(PathParam.class);
        return ParameterInfo.builder()
            .parameter(parameter)
            .type(parameter.getType())
            .index(index)
            .source(ParameterSource.PATH)
            .name(Objects.requireNonNull(ann).value())
            .hints(new String[]{"path"})
            .build();
    }
    
    private ParameterInfo analyzeQueryParam(Parameter parameter, int index) {
        QueryParam ann = parameter.getAnnotation(QueryParam.class);
        return ParameterInfo.builder()
            .parameter(parameter)
            .type(parameter.getType())
            .index(index)
            .source(ParameterSource.QUERY)
            .name(Objects.requireNonNull(ann).value())
            .required(ann.required())
            .defaultValue(ann.defaultValue().isEmpty() ? null : ann.defaultValue())
            .hints(new String[]{"query"})
            .build();
    }
    
    private ParameterInfo analyzeHeader(Parameter parameter, int index) {
        Header ann = parameter.getAnnotation(Header.class);
        return ParameterInfo.builder()
            .parameter(parameter)
            .type(parameter.getType())
            .index(index)
            .source(ParameterSource.HEADER)
            .name(Objects.requireNonNull(ann).value())
            .required(ann.required())
            .hints(new String[]{"header"})
            .build();
    }
    
    private ParameterInfo analyzeRequestBody(Parameter parameter, int index) {
        RequestBody ann = parameter.getAnnotation(RequestBody.class);
        return ParameterInfo.builder()
            .parameter(parameter)
            .type(parameter.getType())
            .index(index)
            .source(ParameterSource.BODY)
            .name("body")
            .required(Objects.requireNonNull(ann).required())
            .hints(new String[]{"body"})
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
