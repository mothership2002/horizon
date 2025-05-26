package horizon.web.http.resolver;

import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.IntentResolver;
import horizon.core.protocol.ProtocolNames;
import horizon.core.security.ProtocolAccessValidator;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP Intent resolver that uses @ProtocolAccess annotations to map HTTP requests to intents.
 */
public class AnnotationBasedHttpIntentResolver implements IntentResolver<FullHttpRequest> {
    private final Map<String, Map<HttpMethod, String>> routeMap = new HashMap<>();
    private final Map<String, Pattern> patternCache = new HashMap<>();
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
        
        // Convert path parameters to regex
        String regexPath = path.replaceAll("\\{([^}]+)}", "([^/]+)");
        Pattern pattern = Pattern.compile("^" + regexPath + "$");
        patternCache.put(path, pattern);
        
        HttpMethod method = HttpMethod.valueOf(methodStr);
        routeMap.computeIfAbsent(path, k -> new HashMap<>()).put(method, intent);
    }
    
    @Override
    public String resolveIntent(FullHttpRequest request) {
        String uri = request.uri().split("\\?")[0];
        HttpMethod method = request.method();
        
        // Try the exact match first
        Map<HttpMethod, String> methodMap = routeMap.get(uri);
        if (methodMap != null && methodMap.containsKey(method)) {
            return methodMap.get(method);
        }
        
        // Try pattern matches
        for (Map.Entry<String, Pattern> entry : patternCache.entrySet()) {
            Matcher matcher = entry.getValue().matcher(uri);
            if (matcher.matches()) {
                Map<HttpMethod, String> methods = routeMap.get(entry.getKey());
                if (methods != null && methods.containsKey(method)) {
                    return methods.get(method);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public boolean canResolve(FullHttpRequest request) {
        return resolveIntent(request) != null;
    }
}
