package horizon.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.protocol.ProtocolAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts HTTP requests and responses to Horizon format.
 */
public class HttpProtocolAdapter implements ProtocolAdapter<FullHttpRequest, FullHttpResponse> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String extractIntent(FullHttpRequest request) {
        // Extract intent from URL path and HTTP method
        String uri = request.uri();
        String path = uri.split("\\?")[0]; // Remove query params
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
        
        // Smart intent extraction based on REST conventions
        String[] parts = path.split("/");
        
        if (parts.length == 1) {
            // Single segment: /users
            String resource = parts[0];
            if (resource.endsWith("s")) {
                resource = resource.substring(0, resource.length() - 1); // Remove plural
            }

            return switch (method) {
                case "GET" -> resource + ".list";
                case "POST" -> resource + ".create";
                default -> resource + "." + method.toLowerCase();
            };
        } else if (parts.length == 2) {
            // Two segments: /users/123 or /users/create
            String resource = parts[0];
            String second = parts[1];
            
            if (resource.endsWith("s")) {
                resource = resource.substring(0, resource.length() - 1); // Remove plural
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
                // It's an action: /users/create, /system/health
                return resource + "." + second;
            }
        } else {
            // Three or more segments: /users/123/orders
            // For now, just use first and last non-numeric parts
            StringBuilder intent = new StringBuilder();
            
            for (String part : parts) {
                if (!part.matches("\\d+")) {
                    if (!intent.isEmpty()) {
                        intent.append(".");
                    }
                    if (part.endsWith("s")) {
                        part = part.substring(0, part.length() - 1); // Remove plural
                    }
                    intent.append(part);
                }
            }
            
            // If we only have one part, add the method
            if (!intent.toString().contains(".")) {
                intent.append(".").append(method.toLowerCase());
            }
            
            return intent.toString();
        }
    }
    
    @Override
    public Object extractPayload(FullHttpRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            
            // Add method
            payload.put("_method", request.method().name());
            
            // Extract path parameters
            String uri = request.uri();
            String[] parts = uri.split("\\?")[0].split("/");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    payload.put("id", Long.parseLong(part));
                }
            }
            
            // Extract query parameters
            QueryStringDecoder queryDecoder = new QueryStringDecoder(uri);
            queryDecoder.parameters().forEach((key, values) -> {
                if (!values.isEmpty()) {
                    payload.put(key, values.size() == 1 ? values.get(0) : values);
                }
            });
            
            // Extract body if present
            if (request.content().readableBytes() > 0) {
                String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                if (contentType != null && contentType.contains("application/json")) {
                    String json = request.content().toString(CharsetUtil.UTF_8);
                    Map<String, Object> body = objectMapper.readValue(json, Map.class);
                    payload.putAll(body);
                }
            }
            
            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract payload", e);
        }
    }
    
    @Override
    public FullHttpResponse buildResponse(Object result, FullHttpRequest request) {
        try {
            String json = objectMapper.writeValueAsString(result);
            ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
            
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                content
            );
            
            response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                .setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            
            return response;
        } catch (Exception e) {
            return buildErrorResponse(e, request);
        }
    }
    
    @Override
    public FullHttpResponse buildErrorResponse(Throwable error, FullHttpRequest request) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", error.getMessage());
        errorBody.put("type", error.getClass().getSimpleName());
        
        try {
            String json = objectMapper.writeValueAsString(errorBody);
            ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
            
            HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            if (error instanceof IllegalArgumentException) {
                status = HttpResponseStatus.BAD_REQUEST;
            }
            
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                content
            );
            
            response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                .setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            
            return response;
        } catch (Exception e) {
            // Fallback error response
            ByteBuf content = Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8);
            return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                content
            );
        }
    }
}
