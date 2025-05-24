package horizon.core.conductor;

import java.lang.reflect.Method;

/**
 * Represents a method within a Conductor that handles a specific intent.
 */
public class ConductorMethod {
    private final Object instance;
    private final Method method;
    private final String intent;
    
    public ConductorMethod(Object instance, Method method, String intent) {
        this.instance = instance;
        this.method = method;
        this.intent = intent;
        this.method.setAccessible(true);
    }
    
    /**
     * Invokes this conductor method with the given payload.
     */
    @SuppressWarnings("unchecked")
    public Object invoke(Object payload) throws Exception {
        // Handle different parameter types
        Class<?>[] paramTypes = method.getParameterTypes();
        
        if (paramTypes.length == 0) {
            // No parameters
            return method.invoke(instance);
        } else if (paramTypes.length == 1) {
            // Single parameter - pass the payload directly
            return method.invoke(instance, payload);
        } else {
            throw new IllegalArgumentException(
                "Conductor method " + method.getName() + " must have 0 or 1 parameter"
            );
        }
    }
    
    public String getIntent() {
        return intent;
    }
    
    public Method getMethod() {
        return method;
    }
}
