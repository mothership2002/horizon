package horizon.core.conductor;

import horizon.core.annotation.*;
import horizon.core.parameter.ParameterHelper;
import horizon.core.parameter.ParameterInfo;
import horizon.core.parameter.ParameterSource;
import horizon.core.util.JsonUtils;
import horizon.core.util.NamingUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a method within a Conductor that handles specific intent.
 * Supports both protocol-neutral @Param and legacy HTTP-specific annotations.
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
     * Supports both protocol-neutral and legacy resolution.
     */
    private Object resolveParameter(ParameterInfo info, Map<String, Object> context) throws Exception {
        Object value = null;

        // Protocol-neutral parameter resolution
        if (info.getSource() == ParameterSource.PARAM || info.getSource() == ParameterSource.AUTO) {
            value = resolveProtocolNeutralParameter(info, context);
        } else {
            // Legacy HTTP-specific resolution (deprecated path)
            value = resolveLegacyParameter(info, context);
        }

        // Validate required parameters
        if (value == null && info.isRequired()) {
            throw new IllegalArgumentException(
                String.format("Required parameter '%s' is missing", info.getName())
            );
        }

        // Apply default value if needed
        if (value == null && info.getDefaultValue() != null) {
            value = info.getDefaultValue();
        }

        // Convert to target type
        if (value != null && !info.getType().isAssignableFrom(value.getClass())) {
            value = JsonUtils.convertValue(value, info.getType());
        }

        return value;
    }

    /**
     * Protocol-neutral parameter resolution.
     * Searches for parameters across all possible locations.
     */
    private Object resolveProtocolNeutralParameter(ParameterInfo info, Map<String, Object> context) {
        String paramName = info.getName();
        String[] hints = info.getHints();
        
        // 1. If hints are provided, search in hinted areas first
        if (hints != null && hints.length > 0) {
            for (String hint : hints) {
                Object value = searchInHintArea(hint, paramName, context);
                if (value != null) return value;
            }
        }
        
        // 2. Perform smart search across all areas
        return smartParameterSearch(paramName, context);
    }

    /**
     * Searches for parameter in a specific hint area.
     */
    private Object searchInHintArea(String hint, String paramName, Map<String, Object> context) {
        switch (hint.toLowerCase()) {
            case "path":
                return context.get("path." + paramName);
            case "query":
                return context.get("query." + paramName);
            case "header":
                return context.get("header." + paramName);
            case "body":
                Object body = context.get("body");
                if (body instanceof Map) {
                    return findInMap((Map<String, Object>) body, paramName);
                }
                return null;
            default:
                // Try direct access for custom hints
                return context.get(hint + "." + paramName);
        }
    }

    /**
     * Smart parameter search across all possible locations.
     */
    private Object smartParameterSearch(String paramName, Map<String, Object> context) {
        // 1. Direct search in common locations
        Object value = directSearch(paramName, context);
        if (value != null) return value;
        
        // 2. Search with name variants
        value = variantSearch(paramName, context);
        if (value != null) return value;
        
        // 3. Deep search in nested structures
        value = deepSearch(paramName, context);
        
        return value;
    }

    /**
     * Direct search in standard locations.
     */
    private Object directSearch(String paramName, Map<String, Object> context) {
        // Check all standard locations
        String[] searchPaths = {
            paramName,                    // Top level
            "path." + paramName,         // HTTP path
            "query." + paramName,        // HTTP query
            "header." + paramName,       // Headers
            "body." + paramName,         // Request body
            "data." + paramName,         // WebSocket data
            "payload." + paramName,      // Generic payload
            "params." + paramName,       // Generic params
        };
        
        for (String path : searchPaths) {
            Object value = context.get(path);
            if (value != null) return value;
        }
        
        return null;
    }

    /**
     * Search with naming convention variants.
     */
    private Object variantSearch(String paramName, Map<String, Object> context) {
        List<String> variants = generateNameVariants(paramName);
        
        for (String variant : variants) {
            Object value = directSearch(variant, context);
            if (value != null) return value;
        }
        
        return null;
    }

    /**
     * Generates name variants for flexible matching.
     */
    private List<String> generateNameVariants(String name) {
        List<String> variants = new ArrayList<>();
        
        // camelCase to snake_case
        String snakeCase = toSnakeCase(name);
        if (!snakeCase.equals(name)) {
            variants.add(snakeCase);
        }
        
        // camelCase to kebab-case
        String kebabCase = toKebabCase(name);
        if (!kebabCase.equals(name)) {
            variants.add(kebabCase);
        }
        
        // Common abbreviations
        if (name.endsWith("Id")) {
            variants.add(name.substring(0, name.length() - 2));  // userId -> user
            variants.add("id");                                   // userId -> id
        }
        
        // Plural handling
        if (name.endsWith("s") && name.length() > 1) {
            variants.add(name.substring(0, name.length() - 1));  // users -> user
        } else {
            variants.add(name + "s");                            // user -> users
        }
        
        // Common field name mappings
        if (name.equals("userId")) {
            variants.add("user_id");
            variants.add("uid");
        }
        
        return variants;
    }

    /**
     * Deep search in nested structures.
     */
    private Object deepSearch(String paramName, Map<String, Object> context) {
        // Search in body
        Object body = context.get("body");
        if (body instanceof Map) {
            Object value = findInMap((Map<String, Object>) body, paramName);
            if (value != null) return value;
        }
        
        // Search in data (WebSocket)
        Object data = context.get("data");
        if (data instanceof Map) {
            Object value = findInMap((Map<String, Object>) data, paramName);
            if (value != null) return value;
        }
        
        // Search in payload
        Object payload = context.get("payload");
        if (payload instanceof Map) {
            Object value = findInMap((Map<String, Object>) payload, paramName);
            if (value != null) return value;
        }
        
        // Search in any Map values at top level
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() instanceof Map && 
                !entry.getKey().startsWith("_")) {  // Skip metadata
                Object value = findInMap((Map<String, Object>) entry.getValue(), paramName);
                if (value != null) return value;
            }
        }
        
        return null;
    }

    /**
     * Finds a parameter in a map with variant support.
     */
    private Object findInMap(Map<String, Object> map, String paramName) {
        // Direct match
        Object value = map.get(paramName);
        if (value != null) return value;
        
        // Try variants
        for (String variant : generateNameVariants(paramName)) {
            value = map.get(variant);
            if (value != null) return value;
        }
        
        return null;
    }

    /**
     * Legacy parameter resolution for backward compatibility.
     * @deprecated Use @Param annotation instead
     */
    @Deprecated
    private Object resolveLegacyParameter(ParameterInfo info, Map<String, Object> context) {
        switch (info.getSource()) {
            case PATH:
                return context.get("path." + info.getName());

            case QUERY:
                Object value = context.get("query." + info.getName());
                if (value == null && info.getDefaultValue() != null) {
                    value = info.getDefaultValue();
                }
                return value;

            case HEADER:
                return context.get("header." + info.getName());

            case BODY:
                value = context.get("body");
                if (value == null) {
                    value = context;
                }
                return value;
                
            default:
                return null;
        }
    }

    /**
     * Converts a string to snake_case.
     */
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * Converts a string to kebab-case.
     */
    private String toKebabCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
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
        return parameters.stream().anyMatch(p -> 
            p.getSource() != ParameterSource.BODY && 
            p.getSource() != ParameterSource.AUTO
        );
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
