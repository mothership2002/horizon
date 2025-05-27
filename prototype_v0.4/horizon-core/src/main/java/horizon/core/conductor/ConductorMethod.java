package horizon.core.conductor;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.annotation.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a method within a Conductor that handles specific intent.
 * Supports both simple single-parameter methods and complex multi-parameter methods
 * with annotations like @PathParam, @QueryParam, @Header.
 */
public class ConductorMethod {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Object instance;
    private final Method method;
    private final String intent;
    private final List<ParameterInfo> parameters;
    
    // Legacy support for single parameter type
    private final Class<?> parameterType;
    
    public ConductorMethod(Object instance, Method method, String intent) {
        this.instance = instance;
        this.method = method;
        this.intent = intent;
        this.method.setAccessible(true);
        this.parameters = analyzeParameters();
        
        // For backward compatibility
        this.parameterType = (parameters.size() == 1 && parameters.get(0).source == ParameterSource.BODY) 
            ? parameters.get(0).type 
            : null;
    }
    
    /**
     * Analyzes method parameters and their annotations.
     */
    private List<ParameterInfo> analyzeParameters() {
        List<ParameterInfo> paramInfos = new ArrayList<>();
        Parameter[] params = method.getParameters();
        
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            ParameterInfo info = new ParameterInfo();
            info.parameter = param;
            info.type = param.getType();
            info.index = i;
            
            // Check for parameter annotations
            if (param.isAnnotationPresent(PathParam.class)) {
                PathParam ann = param.getAnnotation(PathParam.class);
                info.source = ParameterSource.PATH;
                info.name = ann.value();
            } else if (param.isAnnotationPresent(QueryParam.class)) {
                QueryParam ann = param.getAnnotation(QueryParam.class);
                info.source = ParameterSource.QUERY;
                info.name = ann.value();
                info.required = ann.required();
                info.defaultValue = ann.defaultValue().isEmpty() ? null : ann.defaultValue();
            } else if (param.isAnnotationPresent(Header.class)) {
                Header ann = param.getAnnotation(Header.class);
                info.source = ParameterSource.HEADER;
                info.name = ann.value();
                info.required = ann.required();
            } else {
                // No annotation - assume it's the main payload/body
                info.source = ParameterSource.BODY;
                info.name = param.getName(); // May not be available without -parameters
            }
            
            paramInfos.add(info);
        }
        
        return paramInfos;
    }
    
    /**
     * Invokes this conductor method with the given payload.
     * Automatically converts the payload to the correct parameter type.
     * 
     * For backward compatibility with single-parameter methods.
     */
    public Object invoke(Object payload) throws Exception {
        if (parameters.isEmpty()) {
            // No parameters
            return method.invoke(instance);
        }
        
        // Single parameter without annotations (legacy support)
        if (parameters.size() == 1 && parameters.get(0).source == ParameterSource.BODY) {
            Object convertedPayload = convertPayload(payload, parameters.get(0).type);
            return method.invoke(instance, convertedPayload);
        }
        
        // Multiple parameters or annotated parameters
        // Convert payload to context map if it isn't already
        Map<String, Object> context;
        if (payload instanceof Map) {
            context = (Map<String, Object>) payload;
        } else {
            context = Map.of("body", payload);
        }
        
        return invokeWithContext(context);
    }
    
    /**
     * Invokes this conductor method with proper parameter resolution from context.
     * This is the enhanced method that supports multiple parameters.
     */
    public Object invokeWithContext(Map<String, Object> context) throws Exception {
        if (parameters.isEmpty()) {
            return method.invoke(instance);
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
                // For body, we might have the entire payload
                value = context.get("body");
                if (value == null) {
                    // Fallback to the entire context as body (for backward compatibility)
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
            value = objectMapper.convertValue(value, info.type);
        }
        
        return value;
    }
    
    /**
     * Legacy method for converting payload.
     */
    private Object convertPayload(Object payload, Class<?> targetType) throws Exception {
        if (payload == null) {
            return null;
        } else if (targetType.isAssignableFrom(payload.getClass())) {
            // Already the correct type
            return payload;
        } else if (payload instanceof Map && !Map.class.isAssignableFrom(targetType)) {
            // Convert Map to DTO
            return objectMapper.convertValue(payload, targetType);
        } else {
            // Try direct conversion
            return objectMapper.convertValue(payload, targetType);
        }
    }
    
    // Getters
    public String getIntent() {
        return intent;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Class<?> getParameterType() {
        return parameterType;
    }
    
    public List<ParameterInfo> getParameters() {
        return parameters;
    }
    
    public boolean hasAnnotatedParameters() {
        return parameters.stream().anyMatch(p -> p.source != ParameterSource.BODY);
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
