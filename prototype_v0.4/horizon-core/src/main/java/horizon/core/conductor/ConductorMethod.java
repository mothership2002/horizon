package horizon.core.conductor;

import horizon.core.annotation.*;
import horizon.core.parameter.ParameterHelper;
import horizon.core.parameter.ParameterInfo;
import horizon.core.parameter.ParameterSource;
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
        ParameterHelper parameterHelper = new ParameterHelper();
        return parameterHelper.analyze(param, i);
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
            args[paramInfo.getIndex()] = resolveParameter(paramInfo, context);
        }

        return method.invoke(instance, args);
    }

    /**
     * Resolves a single parameter from the context.
     */
    private Object resolveParameter(ParameterInfo info, Map<String, Object> context) throws Exception {
        Object value = null;

        switch (info.getSource()) {
            case PATH:
                value = context.get("path." + info.getName());
                break;

            case QUERY:
                value = context.get("query." + info.getName());
                if (value == null && info.getDefaultValue() != null) {
                    value = info.getDefaultValue();
                }
                break;

            case HEADER:
                value = context.get("header." + info.getName());
                break;

            case BODY:
                value = context.get("body");
                if (value == null) {
                    value = context;
                }
                break;
        }

        // Validate required parameters
        if (value == null && info.isRequired()) {
            throw new IllegalArgumentException(
                String.format("Required parameter '%s' is missing", info.getName())
            );
        }

        // Convert to target type
        if (value != null && !info.getType().isAssignableFrom(value.getClass())) {
            value = JsonUtils.convertValue(value, info.getType());
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
        return parameters.stream().anyMatch(p -> p.getSource() != ParameterSource.BODY);
    }

    /**
     * Gets the single body parameter type if exists.
     * Used for simple DTO conversion.
     */
    public Class<?> getBodyParameterType() {
        return parameters.stream()
            .filter(p -> p.getSource() == ParameterSource.BODY)
            .map(ParameterInfo::getType)
            .findFirst()
            .orElse(null);
    }

}
