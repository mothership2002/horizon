package horizon.core.test;

import horizon.core.HorizonContext;
import horizon.core.conductor.ConductorMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for testing conductors without protocol overhead.
 */
public class ConductorTestHelper {
    
    /**
     * Creates a test context with the given intent and payload.
     */
    public static HorizonContext createContext(String intent, Map<String, Object> payload) {
        HorizonContext context = new HorizonContext();
        context.setIntent(intent);
        context.setPayload(payload);
        context.setAttribute("protocol", "TEST");
        return context;
    }
    
    /**
     * Invokes a conductor method directly with test data.
     */
    public static Object invokeConductor(ConductorMethod method, Map<String, Object> data) 
            throws Exception {
        return method.invoke(data);
    }
    
    /**
     * Creates test payload with common patterns.
     */
    public static class PayloadBuilder {
        private final Map<String, Object> payload = new HashMap<>();
        
        public PayloadBuilder withPath(String name, Object value) {
            payload.put("path." + name, value);
            return this;
        }
        
        public PayloadBuilder withQuery(String name, Object value) {
            payload.put("query." + name, value);
            return this;
        }
        
        public PayloadBuilder withHeader(String name, Object value) {
            payload.put("header." + name, value);
            return this;
        }
        
        public PayloadBuilder withBody(Map<String, Object> body) {
            payload.put("body", body);
            return this;
        }
        
        public PayloadBuilder with(String key, Object value) {
            payload.put(key, value);
            return this;
        }
        
        public Map<String, Object> build() {
            return payload;
        }
    }
}
