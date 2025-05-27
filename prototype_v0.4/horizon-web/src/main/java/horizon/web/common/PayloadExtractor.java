package horizon.web.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import horizon.core.ProtocolAggregator;
import horizon.core.conductor.ConductorMethod;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified payload extractor for web protocols.
 * Automatically converts protocol-specific requests to appropriate DTOs or Maps.
 */
public class PayloadExtractor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    private final ProtocolAggregator aggregator;
    
    public PayloadExtractor(ProtocolAggregator aggregator) {
        this.aggregator = aggregator;
    }
    
    /**
     * Extracts and converts payload for HTTP requests.
     * Automatically uses the correct DTO type based on the conductor method parameter.
     */
    public Object extractHttpPayload(FullHttpRequest request, String intent) {
        try {
            ConductorMethod conductorMethod = getConductorMethod(intent);
            
            // Check if method has annotated parameters
            if (conductorMethod != null && conductorMethod.hasAnnotatedParameters()) {
                // Build context for annotated parameters
                Map<String, Object> context = buildHttpContext(request, intent);
                return context;
            }
            
            // Legacy single-parameter extraction
            Class<?> parameterType = getParameterType(intent);
            
            // Build a unified data structure from all sources
            ObjectNode dataNode = objectMapper.createObjectNode();
            
            // 1. Extract path parameters
            extractPathParameters(request, intent, dataNode);
            
            // 2. Extract query parameters
            extractQueryParameters(request, dataNode);
            
            // 3. Extract request body
            extractRequestBody(request, dataNode);
            
            // 4. Add metadata
            dataNode.put("_method", request.method().name());
            dataNode.put("_uri", request.uri());
            
            // Convert to target type
            if (parameterType != null) {
                // If conductor expects a specific DTO
                return objectMapper.treeToValue(dataNode, parameterType);
            } else {
                // If conductor expects no parameters or uses Object/Map
                return objectMapper.treeToValue(dataNode, Map.class);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract payload for intent: " + intent, e);
        }
    }
    
    /**
     * Builds context map for methods with annotated parameters.
     */
    private Map<String, Object> buildHttpContext(FullHttpRequest request, String intent) throws Exception {
        Map<String, Object> context = new HashMap<>();
        
        // Extract path parameters with proper naming
        extractPathParametersToContext(request, intent, context);
        
        // Extract query parameters with proper naming
        extractQueryParametersToContext(request, context);
        
        // Extract headers
        extractHeadersToContext(request, context);
        
        // Extract body
        extractBodyToContext(request, context);
        
        return context;
    }
    
    /**
     * Extracts and converts payload for WebSocket messages.
     */
    public Object extractWebSocketPayload(Map<String, Object> data, String sessionId, String intent) {
        try {
            // Get the parameter type from conductor method
            Class<?> parameterType = getParameterType(intent);
            
            // Build unified data structure
            Map<String, Object> payload = new HashMap<>();
            if (data != null) {
                payload.putAll(data);
            }
            payload.put("_sessionId", sessionId);
            
            // Convert to target type
            if (parameterType != null && !Map.class.isAssignableFrom(parameterType)) {
                // Convert to specific DTO
                return objectMapper.convertValue(payload, parameterType);
            } else {
                // Return as Map
                return payload;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract WebSocket payload for intent: " + intent, e);
        }
    }
    
    /**
     * Gets the conductor method for an intent.
     */
    private ConductorMethod getConductorMethod(String intent) {
        return aggregator != null ? aggregator.getConductorMethod(intent) : null;
    }
    
    /**
     * Gets the parameter type for a conductor method.
     */
    private Class<?> getParameterType(String intent) {
        ConductorMethod method = getConductorMethod(intent);
        return method != null ? method.getParameterType() : null;
    }
    
    /**
     * Extracts path parameters to context with proper naming convention.
     */
    private void extractPathParametersToContext(FullHttpRequest request, String intent, Map<String, Object> context) {
        String uri = request.uri().split("\\?")[0];
        String[] uriParts = uri.split("/");
        
        // TODO: Get actual path pattern from route configuration
        // For now, extract numeric IDs
        for (String part : uriParts) {
            if (part.matches("\\d+")) {
                context.put("path.id", Long.parseLong(part));
            }
        }
    }
    
    /**
     * Extracts query parameters to context with proper naming convention.
     */
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
    
    /**
     * Extracts headers to context with proper naming convention.
     */
    private void extractHeadersToContext(FullHttpRequest request, Map<String, Object> context) {
        request.headers().forEach(entry -> {
            context.put("header." + entry.getKey(), entry.getValue());
        });
    }
    
    /**
     * Extracts body to context.
     */
    private void extractBodyToContext(FullHttpRequest request, Map<String, Object> context) throws Exception {
        if (request.content().readableBytes() > 0) {
            String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            
            if (contentType != null && contentType.contains("application/json")) {
                String json = request.content().toString(CharsetUtil.UTF_8);
                Object body = objectMapper.readValue(json, Object.class);
                context.put("body", body);
            } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                // Form data
                String formData = request.content().toString(CharsetUtil.UTF_8);
                QueryStringDecoder formDecoder = new QueryStringDecoder("?" + formData, false);
                
                Map<String, Object> formMap = new HashMap<>();
                formDecoder.parameters().forEach((key, values) -> {
                    if (!values.isEmpty()) {
                        formMap.put(key, values.size() == 1 ? values.get(0) : values);
                    }
                });
                context.put("body", formMap);
            }
        }
    }
    
    /**
     * Parses a string value to appropriate type.
     */
    private Object parseValue(String value) {
        // Try to parse as number
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // Not a number
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Boolean.parseBoolean(value);
            }
            return value;
        }
    }
    
    /**
     * Extracts path parameters based on the route pattern.
     */
    private void extractPathParameters(FullHttpRequest request, String intent, ObjectNode dataNode) {
        String uri = request.uri().split("\\?")[0];
        String[] uriParts = uri.split("/");
        
        // Get route pattern from conductor method (if available)
        // For now, use simple ID extraction
        for (String part : uriParts) {
            if (part.matches("\\d+")) {
                dataNode.put("id", Long.parseLong(part));
            }
        }
        
        // TODO: Extract named path parameters based on route pattern
        // e.g., /users/{userId}/posts/{postId}
    }
    
    /**
     * Extracts query parameters from the request.
     */
    private void extractQueryParameters(FullHttpRequest request, ObjectNode dataNode) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        
        queryDecoder.parameters().forEach((key, values) -> {
            if (!values.isEmpty()) {
                if (values.size() == 1) {
                    // Single value - add as string
                    String value = values.get(0);
                    
                    // Try to parse as number if possible
                    try {
                        if (value.contains(".")) {
                            dataNode.put(key, Double.parseDouble(value));
                        } else {
                            dataNode.put(key, Long.parseLong(value));
                        }
                    } catch (NumberFormatException e) {
                        // Not a number, add as string
                        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                            dataNode.put(key, Boolean.parseBoolean(value));
                        } else {
                            dataNode.put(key, value);
                        }
                    }
                } else {
                    // Multiple values - add as array
                    com.fasterxml.jackson.databind.node.ArrayNode arrayNode = dataNode.putArray(key);
                    values.forEach(arrayNode::add);
                }
            }
        });
    }
    
    /**
     * Extracts request body based on content type.
     */
    private void extractRequestBody(FullHttpRequest request, ObjectNode dataNode) throws Exception {
        if (request.content().readableBytes() > 0) {
            String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
            
            if (contentType != null) {
                if (contentType.contains("application/json")) {
                    // JSON body
                    String json = request.content().toString(CharsetUtil.UTF_8);
                    com.fasterxml.jackson.databind.JsonNode bodyNode = objectMapper.readTree(json);
                    
                    if (bodyNode.isObject()) {
                        // Merge all fields from body into dataNode
                        bodyNode.fields().forEachRemaining(entry -> 
                            dataNode.set(entry.getKey(), entry.getValue()));
                    } else if (bodyNode.isArray()) {
                        // If body is an array, add it as _body field
                        dataNode.set("_body", bodyNode);
                    }
                } else if (contentType.contains("application/x-www-form-urlencoded")) {
                    // Form data
                    String formData = request.content().toString(CharsetUtil.UTF_8);
                    QueryStringDecoder formDecoder = new QueryStringDecoder("?" + formData, false);
                    
                    formDecoder.parameters().forEach((key, values) -> {
                        if (!values.isEmpty()) {
                            dataNode.put(key, values.get(0));
                        }
                    });
                }
                // Add support for multipart/form-data if needed
            }
        }
    }
}
