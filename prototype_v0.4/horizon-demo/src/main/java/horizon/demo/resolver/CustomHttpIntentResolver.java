package horizon.demo.resolver;

import horizon.core.protocol.IntentResolver;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom HTTP intent resolver for demo application.
 * Shows how to override default intent mapping.
 */
public class CustomHttpIntentResolver implements IntentResolver<FullHttpRequest> {
    private final Map<String, String> customMappings = new HashMap<>();
    
    public CustomHttpIntentResolver() {
        // Define custom mappings
        customMappings.put("POST:/users/import", "user.import");
        customMappings.put("POST:/users/bulk-create", "user.bulkCreate");
        customMappings.put("GET:/users/search", "user.search");
        customMappings.put("GET:/users/export", "user.export");
        customMappings.put("POST:/users/validate", "user.validate");
        
        // System endpoints
        customMappings.put("GET:/health", "system.health");
        customMappings.put("GET:/metrics", "system.metrics");
        customMappings.put("GET:/", "system.welcome");
    }
    
    @Override
    public String resolveIntent(FullHttpRequest request) {
        String method = request.method().name();
        String path = extractPath(request.uri());
        
        // Check exact match first
        String key = method + ":" + path;
        if (customMappings.containsKey(key)) {
            return customMappings.get(key);
        }
        
        // Check method-agnostic mappings
        if (customMappings.containsKey("*:" + path)) {
            return customMappings.get("*:" + path);
        }
        
        // Return null to let default resolver handle it
        return null;
    }
    
    @Override
    public boolean canResolve(FullHttpRequest request) {
        String method = request.method().name();
        String path = extractPath(request.uri());
        String key = method + ":" + path;
        
        return customMappings.containsKey(key) || 
               customMappings.containsKey("*:" + path);
    }
    
    private String extractPath(String uri) {
        String path = uri.split("\\?")[0];
        if (path.startsWith("/api/")) {
            path = path.substring(5);
        }
        return path;
    }
    
    /**
     * Adds a custom mapping.
     */
    public void addMapping(String method, String path, String intent) {
        customMappings.put(method + ":" + path, intent);
    }
    
    /**
     * Adds a method-agnostic mapping.
     */
    public void addMapping(String path, String intent) {
        customMappings.put("*:" + path, intent);
    }
}
