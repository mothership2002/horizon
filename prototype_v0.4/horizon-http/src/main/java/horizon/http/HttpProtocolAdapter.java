package horizon.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.protocol.ProtocolAdapter;
import horizon.http.resolver.AnnotationBasedHttpIntentResolver;
import horizon.http.resolver.HttpIntentResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts HTTP requests and responses to Horizon format.
 * Now supports both annotation-based and convention-based routing.
 */
public class HttpProtocolAdapter implements ProtocolAdapter<FullHttpRequest, FullHttpResponse> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final HttpIntentResolver fallbackResolver = new HttpIntentResolver();
    private final AnnotationBasedHttpIntentResolver annotationResolver = new AnnotationBasedHttpIntentResolver();
    private final horizon.http.dto.DtoMapper dtoMapper = new horizon.http.dto.DtoMapper();
    
    /**
     * Gets the annotation-based resolver for conductor registration.
     */
    public AnnotationBasedHttpIntentResolver getAnnotationResolver() {
        return annotationResolver;
    }

    /**
     * Registers a DTO mapper configuration.
     */
    public void registerDtoMapper(java.util.function.Consumer<horizon.http.dto.DtoMapper> configurator) {
        configurator.accept(dtoMapper);
    }

    @Override
    public String extractIntent(FullHttpRequest request) {
        // Try annotation-based resolver first
        String intent = annotationResolver.resolveIntent(request);
        if (intent != null) {
            return intent;
        }
        
        // Fall back to convention-based resolver
        return fallbackResolver.resolveIntent(request);
    }

    @Override
    public Object extractPayload(FullHttpRequest request) {
        try {
            String intent = extractIntent(request);

            // Check if we have a registered DTO class for this intent
            if (dtoMapper.hasRequestDtoClass(intent)) {
                Class<?> dtoClass = dtoMapper.getRequestDtoClass(intent);

                // Create a node to build our DTO from
                com.fasterxml.jackson.databind.node.ObjectNode node = objectMapper.createObjectNode();

                // Extract path parameters
                String uri = request.uri();
                String[] parts = uri.split("\\?")[0].split("/");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        node.put("id", Long.parseLong(part));
                    }
                }

                // Extract query parameters
                QueryStringDecoder queryDecoder = new QueryStringDecoder(uri);
                queryDecoder.parameters().forEach((key, values) -> {
                    if (!values.isEmpty()) {
                        if (values.size() == 1) {
                            node.put(key, values.get(0));
                        } else {
                            com.fasterxml.jackson.databind.node.ArrayNode arrayNode = node.putArray(key);
                            values.forEach(arrayNode::add);
                        }
                    }
                });

                // Extract body if present
                if (request.content().readableBytes() > 0) {
                    String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                    if (contentType != null && contentType.contains("application/json")) {
                        String json = request.content().toString(CharsetUtil.UTF_8);
                        com.fasterxml.jackson.databind.JsonNode bodyNode = objectMapper.readTree(json);

                        // Merge body into our node
                        if (bodyNode.isObject()) {
                            bodyNode.fields().forEachRemaining(entry -> 
                                node.set(entry.getKey(), entry.getValue()));
                        }
                    }
                }

                // Convert node to DTO
                return objectMapper.treeToValue(node, dtoClass);
            } else {
                // Fall back to Map for backward compatibility
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
            }
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
            } else if (error instanceof SecurityException) {
                status = HttpResponseStatus.FORBIDDEN;
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
