package horizon.web.grpc;

import horizon.core.ProtocolAggregator;
import horizon.core.protocol.AggregatorAware;
import horizon.core.util.JsonUtils;
import horizon.web.common.AbstractWebProtocolAdapter;
import horizon.web.grpc.resolver.GrpcIntentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Simplified gRPC Protocol Adapter that focuses on DTO-based communication.
 * 
 * Key principles:
 * 1. Java DTOs are the primary data format
 * 2. Protocol Buffers are used only for wire transport
 * 3. JSON is used as intermediate format for conversion
 * 4. No complex type inference - explicit DTO mapping
 */
public class GrpcProtocolAdapter extends AbstractWebProtocolAdapter<GrpcRequest, GrpcResponse>
        implements AggregatorAware {

    private static final Logger logger = LoggerFactory.getLogger(GrpcProtocolAdapter.class);

    private final GrpcIntentResolver intentResolver = new GrpcIntentResolver();
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

            // Extract from JSON payload (converted from protobuf)
            if (request.getJsonPayload() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadData = JsonUtils.fromJson(request.getJsonPayload(), Map.class);

                // Add payload data to context
                context.put("body", payloadData);
                context.putAll(payloadData);  // Also add to root for easier parameter resolution
            }

            // Add gRPC metadata
            if (request.getMetadata() != null && !request.getMetadata().isEmpty()) {
                request.getMetadata().forEach((key, value) -> 
                    context.put("header." + key, value));
            }

            // Add gRPC-specific context
            context.put("_grpcService", request.getServiceName());
            context.put("_grpcMethod", request.getMethodName());
            context.put("_protocol", "gRPC");

            // Check if we need to convert to a DTO
            String intent = intentResolver.resolveIntent(request);
            if (aggregator != null && intent != null) {
                horizon.core.conductor.ConductorMethod conductorMethod = aggregator.getConductorMethod(intent);
                if (conductorMethod != null && !conductorMethod.hasAnnotatedParameters()) {
                    Class<?> bodyType = conductorMethod.getBodyParameterType();
                    if (bodyType != null && !Map.class.isAssignableFrom(bodyType)) {
                        logger.debug("Converting gRPC payload to DTO type: {}", bodyType.getName());
                        // Get the body or use the entire context as source
                        Object body = context.get("body");
                        if (body == null) {
                            body = new HashMap<>(context);
                            // Remove metadata and prefixed keys
                            ((Map<String, Object>) body).entrySet().removeIf(e -> 
                                e.getKey().startsWith("_") || 
                                e.getKey().contains(".")
                            );
                        }
                        return JsonUtils.convertValue(body, bodyType);
                    }
                }
            }

            return context;

        } catch (Exception e) {
            logger.error("Failed to extract payload from gRPC request", e);
            throw new RuntimeException("Failed to extract gRPC payload", e);
        }
    }

    @Override
    protected GrpcResponse doBuildResponse(Object result, GrpcRequest request) {
        try {
            // Convert result to JSON
            String jsonResponse = JsonUtils.toJson(result);

            // Create successful response
            return GrpcResponse.success(jsonResponse);

        } catch (Exception e) {
            logger.error("Failed to build gRPC response", e);
            return doBuildErrorResponse(e, request);
        }
    }

    @Override
    protected GrpcResponse doBuildErrorResponse(Throwable error, GrpcRequest request) {
        logger.error("Building gRPC error response for: {}", error.getMessage());

        // Map Java exceptions to gRPC status codes
        GrpcResponse.Status status = mapExceptionToStatus(error);

        return GrpcResponse.error(status, error.getMessage());
    }

    @Override
    protected GrpcResponse createFallbackErrorResponse(Throwable error, GrpcRequest request) {
        return GrpcResponse.error(
            GrpcResponse.Status.INTERNAL, 
            "Internal server error: " + error.getMessage()
        );
    }

    /**
     * Maps Java exceptions to gRPC status codes.
     */
    private GrpcResponse.Status mapExceptionToStatus(Throwable error) {
        return switch (error) {
            case IllegalArgumentException e -> GrpcResponse.Status.INVALID_ARGUMENT;
            case SecurityException e -> GrpcResponse.Status.PERMISSION_DENIED;
            case UnsupportedOperationException e -> GrpcResponse.Status.UNIMPLEMENTED;
            case IllegalStateException e -> GrpcResponse.Status.FAILED_PRECONDITION;
            case NullPointerException e -> GrpcResponse.Status.INVALID_ARGUMENT;
            default -> {
                if (error.getMessage() != null && error.getMessage().toLowerCase().contains("not found")) {
                    yield GrpcResponse.Status.NOT_FOUND;
                } else {
                    yield GrpcResponse.Status.INTERNAL;
                }
            }
        };
    }
}
