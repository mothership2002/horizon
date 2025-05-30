package horizon.web.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import horizon.core.ProtocolAggregator;
import horizon.core.annotation.GrpcMethod;
import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.AggregatorAware;
import horizon.core.util.JsonUtils;
import horizon.web.common.AbstractWebProtocolAdapter;
import horizon.web.grpc.resolver.GrpcIntentResolver;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adapts gRPC requests and responses to Horizon format.
 * Handles conversion between Protocol Buffers and Horizon's internal format.
 */
public class GrpcProtocolAdapter extends AbstractWebProtocolAdapter<GrpcRequest, GrpcResponse>
        implements AggregatorAware {

    private static final Logger logger = LoggerFactory.getLogger(GrpcProtocolAdapter.class);

    private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser()
            .ignoringUnknownFields();
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
            .includingDefaultValueFields()
            .preservingProtoFieldNames();

    private final GrpcIntentResolver intentResolver = new GrpcIntentResolver();
    private final GrpcMessageConverter messageConverter = new GrpcMessageConverter();
    private ProtocolAggregator aggregator;

    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    protected String doExtractIntent(GrpcRequest request) {
        return intentResolver.resolveIntent(request);
    }

    @Override
    protected Object doExtractPayload(GrpcRequest request) {
        try {
            Map<String, Object> context = new HashMap<>();

            // Try to extract from Protocol Buffer message first
            if (request.message() != null) {
                String json = JSON_PRINTER.print(request.message());
                Map<String, Object> messageData = JsonUtils.fromJson(json, Map.class);

                // Add message data to context
                context.put("body", messageData);

                // Also add fields to root context for easier access
                context.putAll(messageData);
            } else if (request.getRawRequestBytes() != null && !request.getRawRequestBytes().isEmpty()) {
                // Try to parse raw bytes as JSON
                try {
                    String json = request.getRawRequestBytes().toStringUtf8();
                    Map<String, Object> messageData = JsonUtils.fromJson(json, Map.class);
                    context.put("body", messageData);
                    context.putAll(messageData);
                } catch (Exception e) {
                    // If not JSON, store raw bytes
                    context.put("_rawBytes", request.getRawRequestBytes());
                    logger.debug("Could not parse request as JSON, storing raw bytes", e);
                }
            }

            // Add metadata from headers
            Map<String, String> headers = extractHeaders(request);
            if (!headers.isEmpty()) {
                headers.forEach((key, value) -> context.put("header." + key, value));
                context.put("_headers", headers);
            }

            // Add gRPC-specific metadata
            context.put("_grpcService", request.serviceName());
            context.put("_grpcMethod", request.methodName());
            context.put("_grpcFullMethod", request.getFullMethodName());

            if (request.methodDescriptor() != null) {
                context.put("_streaming", request.isStreaming());
                context.put("_methodType", request.methodDescriptor().getType().name());
            }

            return context;

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract payload from gRPC request", e);
        }
    }

    @Override
    protected GrpcResponse doBuildResponse(Object result, GrpcRequest request) {
        try {
            // Check if result is already a Message
            if (result instanceof Message) {
                return GrpcResponse.success((Message) result);
            }

            // Check if result is a GrpcResponse (for custom handling)
            if (result instanceof GrpcResponse) {
                return (GrpcResponse) result;
            }

            // Try to convert to Protocol Buffer if we know the response type
            Message.Builder responseBuilder = getResponseBuilder(request);
            if (responseBuilder != null) {
                String json = JsonUtils.toJson(result);
                JSON_PARSER.merge(json, responseBuilder);
                return GrpcResponse.success(responseBuilder.build());
            }

            // Fallback: Convert result to JSON and return as raw bytes
            String json = JsonUtils.toJson(result);
            ByteString responseBytes = ByteString.copyFromUtf8(json);
            return GrpcResponse.successWithBytes(responseBytes);

        } catch (Exception e) {
            logger.error("Failed to build gRPC response", e);
            return doBuildErrorResponse(e, request);
        }
    }

    @Override
    protected GrpcResponse doBuildErrorResponse(Throwable error, GrpcRequest request) {
        return GrpcResponse.error(error);
    }

    @Override
    protected GrpcResponse createFallbackErrorResponse(Throwable error, GrpcRequest request) {
        return GrpcResponse.error(
                Status.INTERNAL
                        .withDescription("Internal server error")
                        .withCause(error)
        );
    }

    /**
     * Extracts headers from gRPC metadata.
     */
    private Map<String, String> extractHeaders(GrpcRequest request) {
        Map<String, String> headers = new HashMap<>();

        if (request.headers() != null) {
            request.headers().keys().forEach(key -> {
                // Skip binary headers (ending with -bin)
                if (!key.endsWith("-bin")) {
                    String value = request.headers().get(
                            io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)
                    );
                    if (value != null) {
                        headers.put(key, value);
                    }
                }
            });
        }

        return headers;
    }

    /**
     * Gets the response builder for the gRPC method.
     * Uses GrpcServiceRegistry to find the appropriate message type.
     */
    protected Message.Builder getResponseBuilder(GrpcRequest request) {
        String fullMethodName = request.getFullMethodName();

        // First try to get from registry
        GrpcServiceRegistry.MessageTypePair messageTypes =
                GrpcServiceRegistry.getInstance().getMessageTypes(fullMethodName);

        if (messageTypes != null && messageTypes.hasResponseType()) {
            try {
                return messageConverter.getBuilder(messageTypes.responseType());
            } catch (Exception e) {
                logger.error("Failed to create response builder for {}", fullMethodName, e);
            }
        }

        // Try to infer from intent if available
        String intent = doExtractIntent(request);
        if (intent != null && aggregator != null) {
            ConductorMethod conductorMethod = aggregator.getConductorMethod(intent);
            if (conductorMethod != null) {
                // Check for @GrpcMethod annotation
                Method method = conductorMethod.getMethod();
                if (method.isAnnotationPresent(GrpcMethod.class)) {
                    GrpcMethod grpcMethod =
                            method.getAnnotation(GrpcMethod.class);

                    if (Objects.requireNonNull(grpcMethod).responseType() != Object.class
                            && Message.class.isAssignableFrom(grpcMethod.responseType())) {
                        try {
                            return messageConverter.getBuilder(grpcMethod.responseType().asSubclass(Message.class));
                        } catch (Exception e) {
                            logger.error("Failed to create response builder from annotation", e);
                        }
                    }
                }
            }
        }

        return null;
    }
}
