package horizon.web.http.resolver;

import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.IntentResolver;
import horizon.core.protocol.ProtocolNames;
import horizon.core.security.ProtocolAccessValidator;
import horizon.web.common.PathMatcher;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Intent resolver that uses @ProtocolAccess annotations to map HTTP requests to intents.
 * Also extracts path parameters for proper routing.
 */
public class AnnotationBasedHttpIntentResolver implements IntentResolver<FullHttpRequest> {
    private final List<RouteMapping> mappings = new ArrayList<>();
    private final ProtocolAccessValidator accessValidator = new ProtocolAccessValidator();
    
    /**
     * Registers a conductor method with its protocol mappings.
     *
     * @param conductorMethod the conductor method to register
     */
    public void registerConductorMethod(ConductorMethod conductorMethod) {
        Method method = conductorMethod.getMethod();
        String intent = conductorMethod.getIntent();
        
        // Get schema from ProtocolAccessValidator
        String schema = accessValidator.getProtocolSchema(ProtocolNames.HTTP, method);
        if (schema != null && !schema.isEmpty()) {
            parseAndRegisterRoute(schema, intent);
        }
    }
    
    /**
     * Parses a route string and registers it with the intent.
     *
     * @param route the route string (e.g., "GET /users/{id}")
     * @param intent the intent to associate with the route
     */
    private void parseAndRegisterRoute(String route, String intent) {
        String[] parts = route.split(" ", 2);
        if (parts.length != 2) return;
        
        String methodStr = parts[0];
        String path = parts[1];
        
        HttpMethod method = HttpMethod.valueOf(methodStr);
        PathMatcher matcher = new PathMatcher(path);
        
        mappings.add(new RouteMapping(method, matcher, intent));
    }
    
    @Override
    public String resolveIntent(FullHttpRequest request) {
        String uri = request.uri().split("\\?")[0];
        HttpMethod method = request.method();
        
        for (RouteMapping mapping : mappings) {
            if (mapping.method.equals(method)) {
                Map<String, String> params = mapping.pathMatcher.match(uri);
                if (params != null) {
                    // Store path parameters in request for later extraction
                    request.headers().set("X-Path-Params", serializeParams(params));
                    return mapping.intent;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets path parameters from a previous match.
     */
    public static Map<String, String> getPathParams(FullHttpRequest request) {
        String paramsHeader = request.headers().get("X-Path-Params");
        if (paramsHeader == null) {
            return new HashMap<>();
        }
        return deserializeParams(paramsHeader);
    }
    
    private String serializeParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (sb.length() > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        });
        return sb.toString();
    }
    
    private static Map<String, String> deserializeParams(String params) {
        Map<String, String> result = new HashMap<>();
        if (params != null && !params.isEmpty()) {
            for (String pair : params.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    result.put(kv[0], kv[1]);
                }
            }
        }
        return result;
    }
    
    /**
     * Route mapping entry.
     */
    private static class RouteMapping {
        final HttpMethod method;
        final PathMatcher pathMatcher;
        final String intent;
        
        RouteMapping(HttpMethod method, PathMatcher pathMatcher, String intent) {
            this.method = method;
            this.pathMatcher = pathMatcher;
            this.intent = intent;
        }
    }
}
