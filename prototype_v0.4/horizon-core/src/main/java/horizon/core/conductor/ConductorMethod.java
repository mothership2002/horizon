package horizon.core.conductor;

import horizon.core.annotation.*;
import horizon.core.util.JsonUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a method within a Conductor that handles specific intent.
 * Supports annotated parameters like @PathParam, @QueryParam, @Header.
 */
public class ConductorMethod {
    private final Object instance;
    private final Method method;
    private final String intent;
    private final List<ParameterInfo> parameters;

    public ConductorMethod(Object instance, Method method, String intent) {
        this.instance = instance;
        this.method = method;
        this.intent = intent;
        this.method.setAccessible(true);
        this.parameters = analyzeParameters();
    }

    /**
     * Analyzes method parameters and their annotations.
     */
    private List<ParameterInfo> analyzeParameters() {
        List<ParameterInfo> paramInfos = new ArrayList<>();
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            paramInfos.add(analyzeParameter(params[i], i));
        }

        return paramInfos;
    }

    private ParameterInfo analyzeParameter(Parameter param, int i) {
        ParameterInfo info = new ParameterInfo();

        info.parameter = param;
        info.type = param.getType();
        info.index = i;
//        Annotation[] declaredAnnotations = param.getDeclaredAnnotations();
        // Check for parameter annotations
        if (param.isAnnotationPresent(PathParam.class)) {
            PathParam ann = param.getAnnotation(PathParam.class);
            info.source = ParameterSource.PATH;
            info.name = Objects.requireNonNull(ann).value();
        } else if (param.isAnnotationPresent(QueryParam.class)) {
            QueryParam ann = param.getAnnotation(QueryParam.class);
            info.source = ParameterSource.QUERY;
            info.name = Objects.requireNonNull(ann).value();
            info.required = ann.required();
            info.defaultValue = ann.defaultValue().isEmpty() ? null : ann.defaultValue();
        } else if (param.isAnnotationPresent(Header.class)) {
            Header ann = param.getAnnotation(Header.class);
            info.source = ParameterSource.HEADER;
            info.name = Objects.requireNonNull(ann).value();
            info.required = ann.required();
        } else {
            // No annotation - assume it's the main payload/body
            info.source = ParameterSource.BODY;
            info.name = param.getName();
        }
        return info;
    }

    /**
     * Invokes this conductor method with proper parameter resolution.
     */
    public Object invoke(Object payload) throws Exception {
        if (parameters.isEmpty()) {
            return method.invoke(instance);
        }

        // Convert payload to context map
        Map<String, Object> context;
        if (payload instanceof Map) {
            context = (Map<String, Object>) payload;
        } else {
            context = Map.of("body", payload);
        }

        Object[] args = new Object[parameters.size()];

        for (ParameterInfo paramInfo : parameters) {
            args[paramInfo.index] = resolveParameter(paramInfo, context);
        }

        return method.invoke(instance, args);
    }

    /**
     * Resolves a single parameter from the context.
     */
    private Object resolveParameter(ParameterInfo info, Map<String, Object> context) throws Exception {
        Object value = null;

        switch (info.source) {
            case PATH:
                value = context.get("path." + info.name);
                break;

            case QUERY:
                value = context.get("query." + info.name);
                if (value == null && info.defaultValue != null) {
                    value = info.defaultValue;
                }
                break;

            case HEADER:
                value = context.get("header." + info.name);
                break;

            case BODY:
                value = context.get("body");
                if (value == null) {
                    value = context;
                }
                break;
        }

        // Validate required parameters
        if (value == null && info.required) {
            throw new IllegalArgumentException(
                String.format("Required parameter '%s' is missing", info.name)
            );
        }

        // Convert to target type
        if (value != null && !info.type.isAssignableFrom(value.getClass())) {
            value = JsonUtils.convertValue(value, info.type);
        }

        return value;
    }

    // Getters
    public String getIntent() {
        return intent;
    }

    public Method getMethod() {
        return method;
    }

    public List<ParameterInfo> getParameters() {
        return parameters;
    }

    public boolean hasAnnotatedParameters() {
        return parameters.stream().anyMatch(p -> p.source != ParameterSource.BODY);
    }

    /**
     * Gets the single body parameter type if exists.
     * Used for simple DTO conversion.
     */
    public Class<?> getBodyParameterType() {
        return parameters.stream()
            .filter(p -> p.source == ParameterSource.BODY)
            .map(p -> p.type)
            .findFirst()
            .orElse(null);
    }

    /**
     * Information about a method parameter.
     */
    public static class ParameterInfo {
        public Parameter parameter;
        public Class<?> type;
        public int index;
        public ParameterSource source;
        public String name;
        public boolean required = true;
        public String defaultValue;
    }

    /**
     * Source of parameter value.
     */
    public enum ParameterSource {
        PATH,    // From URL path
        QUERY,   // From query string
        HEADER,  // From HTTP header
        BODY     // From request body
    }
}
