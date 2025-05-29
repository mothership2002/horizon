package horizon.web.http;

import horizon.core.ProtocolAggregator;
import horizon.core.protocol.AggregatorAware;
import horizon.core.util.JsonUtils;
import horizon.web.common.AbstractWebProtocolAdapter;
import horizon.web.common.PayloadExtractor;
import horizon.web.http.resolver.HttpIntentResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts HTTP requests and responses to Horizon format.
 * This class extends AbstractWebProtocolAdapter to provide HTTP-specific functionality.
 * Uses JsonUtils for JSON operations.
 */
public abstract class HttpProtocolAdapter extends AbstractWebProtocolAdapter<FullHttpRequest, FullHttpResponse>
        implements AggregatorAware {
    private final HttpIntentResolver intentResolver = new HttpIntentResolver();
    private PayloadExtractor payloadExtractor;

    /**
     * Sets the protocol aggregator for accessing conductor metadata.
     */
    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        this.payloadExtractor = new PayloadExtractor(aggregator);
    }

    @Override
    protected String doExtractIntent(FullHttpRequest request) {
        return intentResolver.resolveIntent(request);
    }

    @Override
    protected Object doExtractPayload(FullHttpRequest request) {
        String intent = extractIntent(request);

        if (payloadExtractor != null) {
            // Use the unified payload extractor
            return payloadExtractor.extractHttpPayload(request, intent);
        } else {
            // Fallback to simple Map extraction if aggregator not set
            return extractPayloadAsMap(request);
        }
    }

    /**
     * Simple payload extraction as Map (fallback method).
     */
    private Map<String, Object> extractPayloadAsMap(FullHttpRequest request) {
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
                    payload.put(key, values.size() == 1 ? values.getFirst() : values);
                }
            });

            // Extract body if present
            if (request.content().readableBytes() > 0) {
                String contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
                if (contentType != null && contentType.contains("application/json")) {
                    String json = request.content().toString(CharsetUtil.UTF_8);
                    payload.putAll(JsonUtils.fromJson(json, Map.class));
                }
            }

            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract payload", e);
        }
    }

    @Override
    protected FullHttpResponse doBuildResponse(Object result, FullHttpRequest request) {
        try {
            String json = JsonUtils.toJson(result);
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
            throw new RuntimeException("Failed to build response", e);
        }
    }

    @Override
    protected FullHttpResponse doBuildErrorResponse(Throwable error, FullHttpRequest request) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", error.getMessage());
        errorBody.put("type", error.getClass().getSimpleName());

        try {
            String json = JsonUtils.toJson(errorBody);
            ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);

            FullHttpResponse response = getFullHttpResponse(error, content);

            response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                .setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

            return response;
        } catch (Exception e) {
            return createFallbackErrorResponse(e, request);
        }
    }

    private FullHttpResponse getFullHttpResponse(Throwable error, ByteBuf content) {
        HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        if (error instanceof IllegalArgumentException) {
            status = HttpResponseStatus.BAD_REQUEST;
        } else if (error instanceof SecurityException) {
            status = HttpResponseStatus.FORBIDDEN;
        }

        return new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            content
        );
    }

    @Override
    protected FullHttpResponse createFallbackErrorResponse(Throwable error, FullHttpRequest request) {
        // Fallback error response
        ByteBuf content = Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            content
        );

        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
            .setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        return response;
    }
}
