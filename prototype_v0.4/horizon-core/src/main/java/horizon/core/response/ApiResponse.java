package horizon.core.response;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard API response wrapper for consistent responses across all protocols.
 */
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final Map<String, Object> metadata;
    private final Instant timestamp;
    
    private ApiResponse(Builder<T> builder) {
        this.success = builder.success;
        this.data = builder.data;
        this.message = builder.message;
        this.metadata = builder.metadata;
        this.timestamp = Instant.now();
    }
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) {
        return new Builder<T>()
            .success(true)
            .data(data)
            .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new Builder<T>()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }
    
    public static ApiResponse<Void> error(String message) {
        return new Builder<Void>()
            .success(false)
            .message(message)
            .build();
    }
    
    public static ApiResponse<Map<String, String>> validationError(Map<String, String> errors) {
        return new Builder<Map<String, String>>()
            .success(false)
            .data(errors)
            .message("Validation failed")
            .build();
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Instant getTimestamp() { return timestamp; }
    
    // Convert to Map for protocols that need it
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        map.put("data", data);
        map.put("message", message);
        map.put("metadata", metadata);
        map.put("timestamp", timestamp.toString());
        return map;
    }
    
    // Builder
    public static class Builder<T> {
        private boolean success;
        private T data;
        private String message;
        private Map<String, Object> metadata = new HashMap<>();
        
        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }
        
        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder<T> metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }
}
