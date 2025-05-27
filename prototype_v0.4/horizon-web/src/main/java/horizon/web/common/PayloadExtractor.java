package horizon.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.ProtocolAggregator;
import horizon.core.conductor.ConductorMethod;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Unified payload extractor for web protocols.
 * Converts protocol-specific requests to context maps for ConductorMethod.
 */
public class PayloadExtractor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ProtocolAggregator aggregator;
    
    public PayloadExtractor(ProtocolAggregator aggregator) {
        this.aggregator = aggregator;
    }
    
    /**
     * Extracts payload for HTTP requests.
     * Always returns a Map with proper context structure.
     */
    public Object extractHttpPayload(FullHttpRequest request, String intent) {
        try {
            Map<String, Object> context = new HashMap<>();
            
            // Extract all components
            extractPathParametersToContext(request, intent, context);
            extractQueryParametersToContext(request, context);
            extractHeadersToContext(request, context);
            extractBodyToContext(request, context);
            
            // Add metadata
            context.put("_method", request.method().name());
            context.put("_uri", request.uri());
            
            // Get conductor method to check if we need simple DTO conversion
            ConductorMethod conductorMethod = getConductorMethod(intent);
            if (conductorMethod != null && !conductorMethod.hasAnnotatedParameters()) {
                Class<?> bodyType = conductorMethod.getBodyParameterType();
                if (bodyType != null && !Map.class.isAssignableFrom(bodyType)) {
                    // Convert the body to the expected DTO type
                    Object body = context.get("body");
                    if (body == null) {
                        // If no body, use entire context as source
                        body = new HashMap<>(context);
                        // Remove metadata and prefixed keys
                        ((Map<String, Object>) body).entrySet().removeIf(e -> 
                            e.getKey().startsWith("_") || 
                            e.getKey().contains(".")
                        );
                    }
                    return objectMapper.convertValue(body, bodyType);
                }
            }
            
            return context;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract payload for intent: " + intent, e);
        }
    }
    
    /**
     * Extracts payload for WebSocket messages.
     */
    public Object extractWebSocketPayload(Map<String, Object> data, String sessionId, String intent) {
        try {
            Map<String, Object> context = new HashMap<>();
            
            // Add all data with proper prefixes
            if (data != null) {
                data.forEach((key, value) -> {
                    // Separate query parameters from body
                    if (isQueryParam(key)) {
                        context.put("query." + key, value);
                    } else {
                        context.put(key, value);
                    }
                });
            }
            
            // Add session ID
            context.put("_sessionId", sessionId);
            
            // Create body from non-prefixed data
            Map<String, Object> body = new HashMap<>();
            context.forEach((key, value) -> {
                if (!key.contains(".") && !key.startsWith("_")) {
                    body.put(key, value);
                }
            });
            if (!body.isEmpty()) {
                context.put("body", body);
            }
            
            // Check if we need DTO conversion
            ConductorMethod conductorMethod = getConductorMethod(intent);
            if (conductorMethod != null && !conductorMethod.hasAnnotatedParameters()) {
                Class<?> bodyType = conductorMethod.getBodyParameterType();
                if (bodyType != null && !Map.class.isAssignableFrom(bodyType)) {
                    return objectMapper.convertValue(body.isEmpty() ? data : body, bodyType);
                }
            }
            
            return context;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract WebSocket payload for intent: " + intent, e);
        }
    }
    
    private ConductorMethod getConductorMethod(String intent) {
        return aggregator != null ? aggregator.getConductorMethod(intent) : null;
    }
    
    private void extractPathParametersToContext(FullHttpRequest request, String intent, Map<String, Object> context) {
        String uri = request.uri().split("\\?")[0];
        
        // TODO: Get actual route pattern and extract named parameters
        // For now, extract numeric IDs
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.matches("\\d+")) {
                // Check if previous part might be the parameter name
                if (i > 0) {
                    String prevPart = parts[i-1];
                    if (prevPart.endsWith("s")) {
                        // users/123 -> userId = 123
                        String paramName = prevPart.substring(0, prevPart.length() - 1) + "Id";
                        context.put("path." + paramName, Long.parseLong(part));
                    }
                }
                // Always add as generic 'id'
                context.put("path.id", Long.parseLong(part));
            }
        }
    }
    
    private void extractQueryParametersToContext(FullHttpRequest request, Map<String, Object> context) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        
        queryDecoder.parameters().forEach((key, values) -> {
            if (!values.isEmpty()) {
                if (values.size() == 1) {
                    context.put("query." + key, parseValue(values.get(0)));
                } else {
                    context.put("query." + key, values.toArray(new String[0]));
                }
            }
        });
    }
    
    private void extractHeadersToContext(FullHttpRequest request, Map<String, Object> context) {
        request.headers().forEach(entry -> {
            context.put("header." + entry.getKey(), entry.getValue());
        });
    }
    
    private void extractBodyToContext(FullHttpRequest request, Map<String, Object> context) throws Exception {
        if (request.content().readableBytes() > 0) {
            String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            
            if (contentType != null && contentType.contains("application/json")) {
                String json = request.content().toString(CharsetUtil.UTF_8);
                Object body = objectMapper.readValue(json, Object.class);
                context.put("body", body);
                
                // Also add body fields to root context for DTO mapping
                if (body instanceof Map) {
                    ((Map<String, Object>) body).forEach((key, value) -> {
                        if (!context.containsKey(key)) {
                            context.put(key, value);
                        }
                    });
                }
            } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                String formData = request.content().toString(CharsetUtil.UTF_8);
                QueryStringDecoder formDecoder = new QueryStringDecoder("?" + formData, false);
                
                Map<String, Object> formMap = new HashMap<>();
                formDecoder.parameters().forEach((key, values) -> {
                    if (!values.isEmpty()) {
                        Object value = values.size() == 1 ? parseValue(values.get(0)) : values;
                        formMap.put(key, value);
                        context.put(key, value);
                    }
                });
                context.put("body", formMap);
            }
        }
    }
    
    private Object parseValue(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            return value;
        }
    }
    
    private boolean isQueryParam(String key) {
        // Common query parameter patterns
        return key.matches("page|size|limit|offset|sort|order|filter|q|query|search");
    }
}
