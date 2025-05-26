package horizon.core.conductor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Represents a method within a Conductor that handles a specific intent.
 */
public class ConductorMethod {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Object instance;
    private final Method method;
    private final String intent;
    private final Class<?> parameterType;
    
    public ConductorMethod(Object instance, Method method, String intent) {
        this.instance = instance;
        this.method = method;
        this.intent = intent;
        this.method.setAccessible(true);
        
        // Determine parameter type
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 1) {
            this.parameterType = parameters[0].getType();
        } else if (parameters.length == 0) {
            this.parameterType = null;
        } else {
            throw new IllegalArgumentException(
                "Conductor method " + method.getName() + " must have 0 or 1 parameter"
            );
        }
    }
    
    /**
     * Invokes this conductor method with the given payload.
     * Automatically converts the payload to the correct parameter type.
     */
    @SuppressWarnings("unchecked")
    public Object invoke(Object payload) throws Exception {
        if (parameterType == null) {
            // No parameters
            return method.invoke(instance);
        }
        
        // Convert payload to the correct type
        Object convertedPayload;
        
        if (payload == null) {
            convertedPayload = null;
        } else if (parameterType.isAssignableFrom(payload.getClass())) {
            // Already the correct type
            convertedPayload = payload;
        } else if (payload instanceof Map && !Map.class.isAssignableFrom(parameterType)) {
            // Convert Map to DTO
            convertedPayload = objectMapper.convertValue(payload, parameterType);
        } else {
            // Try direct conversion
            convertedPayload = objectMapper.convertValue(payload, parameterType);
        }
        
        return method.invoke(instance, convertedPayload);
    }
    
    public String getIntent() {
        return intent;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public Class<?> getParameterType() {
        return parameterType;
    }
}
