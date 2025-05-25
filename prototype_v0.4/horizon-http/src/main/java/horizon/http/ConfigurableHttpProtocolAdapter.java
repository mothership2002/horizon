package horizon.http;

import horizon.core.protocol.IntentResolver;
import horizon.core.protocol.ProtocolAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable HTTP protocol adapter that allows custom intent resolution strategies.
 */
public class ConfigurableHttpProtocolAdapter extends HttpProtocolAdapter {
    private final List<IntentResolver<FullHttpRequest>> resolvers = new ArrayList<>();
    private final IntentResolver<FullHttpRequest> defaultResolver;
    
    public ConfigurableHttpProtocolAdapter() {
        // Default resolver uses the existing smart extraction logic
        this.defaultResolver = new DefaultHttpIntentResolver();
    }
    
    /**
     * Adds a custom intent resolver with higher priority than the default.
     */
    public void addResolver(IntentResolver<FullHttpRequest> resolver) {
        resolvers.addFirst(resolver); // Add at beginning for priority
    }
    
    @Override
    public String extractIntent(FullHttpRequest request) {
        // Try custom resolvers first
        for (IntentResolver<FullHttpRequest> resolver : resolvers) {
            if (resolver.canResolve(request)) {
                String intent = resolver.resolveIntent(request);
                if (intent != null) {
                    return intent;
                }
            }
        }
        
        // Fall back to default resolver
        return defaultResolver.resolveIntent(request);
    }
    
    /**
     * Default intent resolver that implements the smart REST-style mapping.
     */
    private static class DefaultHttpIntentResolver implements IntentResolver<FullHttpRequest> {
        @Override
        public String resolveIntent(FullHttpRequest request) {
            String uri = request.uri();
            String path = uri.split("\\?")[0];
            String method = request.method().name();
            
            // Remove /api prefix if present
            if (path.startsWith("/api/")) {
                path = path.substring(5);
            } else if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            // Handle root path
            if (path.isEmpty()) {
                return "system.welcome";
            }
            
            String[] parts = path.split("/");
            String resource = parts[0];

            if (parts.length == 1) {
                // Single segment: /users
                if (resource.endsWith("s")) {
                    resource = resource.substring(0, resource.length() - 1);
                }

                return switch (method) {
                    case "GET" -> resource + ".list";
                    case "POST" -> resource + ".create";
                    default -> resource + "." + method.toLowerCase();
                };
            } else if (parts.length == 2) {
                // Two segments: /users/123 or /users/search
                String second = parts[1];
                
                if (resource.endsWith("s")) {
                    resource = resource.substring(0, resource.length() - 1);
                }
                
                // Check if second part is a number (ID)
                if (second.matches("\\d+")) {
                    return switch (method) {
                        case "GET" -> resource + ".get";
                        case "PUT", "PATCH" -> resource + ".update";
                        case "DELETE" -> resource + ".delete";
                        default -> resource + "." + method.toLowerCase();
                    };
                } else {
                    // It's an action: /users/search, /users/export
                    return resource + "." + second;
                }
            } else {
                // Three or more segments
                StringBuilder intent = new StringBuilder();
                
                for (String part : parts) {
                    if (!part.matches("\\d+")) {
                        if (!intent.isEmpty()) {
                            intent.append(".");
                        }
                        if (part.endsWith("s")) {
                            part = part.substring(0, part.length() - 1);
                        }
                        intent.append(part);
                    }
                }
                
                return intent.toString();
            }
        }
    }
}
