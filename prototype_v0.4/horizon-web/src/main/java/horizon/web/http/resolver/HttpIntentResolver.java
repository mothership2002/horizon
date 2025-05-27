package horizon.web.http.resolver;

import horizon.core.protocol.IntentResolver;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Default intent resolver for HTTP requests using REST conventions.
 */
public class HttpIntentResolver implements IntentResolver<FullHttpRequest> {
    
    @Override
    public String resolveIntent(FullHttpRequest request) {
        String uri = request.uri();
        String path = uri.split("\\?")[0];
        String method = request.method().name();
        
        // Remove leading slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        // Remove /api prefix if present
        if (path.startsWith("api/")) {
            path = path.substring(4);
        }
        
        // Handle root path
        if (path.isEmpty()) {
            return "system.welcome";
        }
        
        String[] parts = path.split("/");
        
        if (parts.length == 1) {
            // Single segment: /users
            String resource = normalizeResource(parts[0]);
            return switch (method) {
                case "GET" -> resource + ".list";
                case "POST" -> resource + ".create";
                default -> resource + "." + method.toLowerCase();
            };
        } else if (parts.length == 2) {
            // Two segments: /users/123 or /users/search
            String resource = normalizeResource(parts[0]);
            String second = parts[1];
            
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
            // Three or more segments - build intent from non-numeric parts
            StringBuilder intent = new StringBuilder();
            for (String part : parts) {
                if (!part.matches("\\d+")) {
                    if (!intent.isEmpty()) {
                        intent.append(".");
                    }
                    intent.append(normalizeResource(part));
                }
            }
            return intent.toString();
        }
    }
    
    private String normalizeResource(String resource) {
        // Remove trailing 's' for plurals
        if (resource.endsWith("s") && resource.length() > 1) {
            return resource.substring(0, resource.length() - 1);
        }
        return resource;
    }
}
