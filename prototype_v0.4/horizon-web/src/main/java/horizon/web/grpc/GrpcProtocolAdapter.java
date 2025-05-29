package horizon.web.grpc;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import horizon.core.ProtocolAggregator;
import horizon.core.protocol.AggregatorAware;
import horizon.core.util.JsonUtils;
import horizon.web.common.AbstractWebProtocolAdapter;
import io.grpc.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts gRPC requests and responses to Horizon format.
 * Handles conversion between Protocol Buffers and Horizon's internal format.
 */
public class GrpcProtocolAdapter extends AbstractWebProtocolAdapter<GrpcRequest, GrpcResponse>
        implements AggregatorAware {

    private static final JsonFormat.Parser jsonParser = JsonFormat.parser().ignoringUnknownFields();
    private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer().includingDefaultValueFields();

    private ProtocolAggregator aggregator;

    @Override
    public void setProtocolAggregator(ProtocolAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @Override
    protected String doExtractIntent(GrpcRequest request) {
        // Convert gRPC service/method to intent
        // Example: UserService/CreateUser -> user.create
        String serviceName = request.serviceName().toLowerCase();
        String methodName = request.methodName();

        // Remove the "Service" suffix if present
        if (serviceName.endsWith("service")) {
            serviceName = serviceName.substring(0, serviceName.length() - 7);
        }

        // Convert CamelCase method to lowercase
        String action = camelToLowerCase(methodName);

        return serviceName + "." + action;
    }

    @Override
    protected Object doExtractPayload(GrpcRequest request) {
        try {
            // Convert Protocol Buffer message to Map
            Message message = request.message();
            String json = jsonPrinter.print(message);
            Map<String, Object> payload = JsonUtils.fromJson(json, Map.class);

            // Add metadata from headers
            if (request.headers() != null) {
                Map<String, String> headers = extractHeaders(request);
                payload.put("_headers", headers);
            }

            // Add method information
            payload.put("_grpcMethod", request.getFullMethodName());
            payload.put("_streaming", request.isStreaming());

            return payload;

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

            // Convert result to JSON then to Protocol Buffer
            String json = JsonUtils.toJson(result);

            // Get the response type from method descriptor
            Message.Builder responseBuilder = getResponseBuilder(request);
            if (responseBuilder != null) {
                jsonParser.merge(json, responseBuilder);
                return GrpcResponse.success(responseBuilder.build());
            }

            // Fallback: wrap in generic response
            return GrpcResponse.success(createGenericResponse(result));

        } catch (Exception e) {
            return GrpcResponse.error(
                Status.INTERNAL.withDescription("Failed to build response: " + e.getMessage())
            );
        }
    }

    @Override
    protected GrpcResponse doBuildErrorResponse(Throwable error, GrpcRequest request) {
        return GrpcResponse.error(error);
    }

    @Override
    protected GrpcResponse createFallbackErrorResponse(Throwable error, GrpcRequest request) {
        return GrpcResponse.error(
            Status.INTERNAL.withDescription("Internal server error")
        );
    }

    /**
     * Extracts headers from gRPC metadata.
     */
    private Map<String, String> extractHeaders(GrpcRequest request) {
        Map<String, String> headers = new HashMap<>();

        if (request.headers() != null) {
            request.headers().keys().forEach(key -> {
                String value = request.headers().get(
                    io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)
                );
                if (value != null) {
                    headers.put(key, value);
                }
            });
        }

        return headers;
    }

    /**
     * Converts CamelCase to lowercase with dots.
     * Example: CreateUser -> create.user, GetUserById -> get.user.by.id
     */
    private String camelToLowerCase(String camelCase) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append('.');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    /**
     * Gets the response builder for the gRPC method.
     * This would need to be implemented based on your proto definitions.
     */
    private Message.Builder getResponseBuilder(GrpcRequest request) {
        // This is a simplified version. In practice, you would:
        // 1. Use reflection on the method descriptor
        // 2. Or maintain a registry of response types
        // 3. Or use code generation

        // For now, return null to use generic response
        return null;
    }

    /**
     * Creates a generic response message.
     * This would typically use a generic proto message type.
     */
    private Message createGenericResponse(Object result) {
        // In a real implementation, you would have a generic proto message
        // For now, we'll throw an exception
        throw new UnsupportedOperationException(
            "Generic gRPC response not implemented. Please return a proper Protocol Buffer message."
        );
    }
}
