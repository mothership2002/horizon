package horizon.web.http.resolver;

import horizon.core.protocol.IntentResolver;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Common implementation of intent resolution logic for HTTP requests.
 * This class extracts intents from HTTP requests based on REST conventions.
 */
public class HttpIntentResolver implements IntentResolver<FullHttpRequest> {
    
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
    
    @Override
    public boolean canResolve(FullHttpRequest request) {
        return true; // This resolver can handle any HTTP request
    }
}