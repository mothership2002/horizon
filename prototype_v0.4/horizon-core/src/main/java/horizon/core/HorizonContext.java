package horizon.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The context that flows through the Horizon framework.
 * Contains all information about a request as it travels from protocol to response.
 */
public class HorizonContext {
    private final String traceId;
    private final Instant timestamp;
    private final Map<String, Object> attributes;
    
    private String intent;
    private Object payload;
    private Object result;
    private Throwable error;
    
    public HorizonContext() {
        this.traceId = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.attributes = new HashMap<>();
    }
    
    // Getters and setters
    public String getTraceId() {
        return traceId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public Object getResult() {
        return result;
    }
    
    public void setResult(Object result) {
        this.result = result;
    }
    
    public boolean hasError() {
        return error != null;
    }
    
    public Throwable getError() {
        return error;
    }
    
    public void setError(Throwable error) {
        this.error = error;
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        return (T) attributes.get(key);
    }
}
