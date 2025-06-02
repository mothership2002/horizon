package horizon.web.grpc;

import horizon.core.HorizonContext;
import horizon.web.common.AbstractFoyer;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Simplified gRPC Foyer that focuses on JSON-based communication.
 * 
 * This implementation:
 * 1. Accepts any gRPC method call
 * 2. Converts protobuf to JSON automatically
 * 3. Routes to Horizon conductors
 * 4. Converts JSON responses back to protobuf
 * 
 * No need for pre-defined .proto files or generated stubs.
 */
public class GrpcFoyer extends AbstractFoyer<GrpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(GrpcFoyer.class);

    private Server grpcServer;
    private final GrpcConfiguration configuration;

    public GrpcFoyer(int port) {
        this(port, GrpcConfiguration.defaultConfig());
    }

    public GrpcFoyer(int port, GrpcConfiguration configuration) {
        super(port);
        this.configuration = configuration;
    }

    @Override
    protected String getProtocolName() {
        return "gRPC";
    }

    @Override
    public void open() {
        if (isOpen.compareAndSet(false, true)) {
            logger.info("Opening gRPC Foyer on port {}", port);

            try {
                ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
                
                // Apply basic configuration
                serverBuilder.maxInboundMessageSize(configuration.getMaxInboundMessageSize());
                serverBuilder.maxInboundMetadataSize(configuration.getMaxInboundMetadataSize());
                
                // Add the universal handler for all methods
                serverBuilder.fallbackHandlerRegistry(new UniversalHandlerRegistry());
                
                grpcServer = serverBuilder.build().start();

                logger.info("gRPC Foyer opened successfully on port {}", port);

            } catch (IOException e) {
                logger.error("Failed to start gRPC server", e);
                close();
                throw new RuntimeException("Failed to start gRPC server", e);
            }
        }
    }

    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            logger.info("Closing gRPC Foyer");

            if (grpcServer != null) {
                try {
                    grpcServer.shutdown();
                    if (!grpcServer.awaitTermination(30, TimeUnit.SECONDS)) {
                        grpcServer.shutdownNow();
                        if (!grpcServer.awaitTermination(10, TimeUnit.SECONDS)) {
                            logger.error("gRPC server did not terminate");
                        }
                    }
                } catch (InterruptedException e) {
                    grpcServer.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            logger.info("gRPC Foyer closed");
        }
    }

    /**
     * Universal handler registry that handles all gRPC methods dynamically.
     */
    private class UniversalHandlerRegistry extends HandlerRegistry {
        
        @Override
        public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
            logger.debug("Looking up gRPC method: {} for authority: {}", methodName, authority);
            
            // Create a universal method definition that accepts and returns JSON strings
            MethodDescriptor<String, String> methodDescriptor = MethodDescriptor.<String, String>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(methodName)
                .setRequestMarshaller(new JsonStringMarshaller())
                .setResponseMarshaller(new JsonStringMarshaller())
                .build();
            
            return ServerMethodDefinition.create(methodDescriptor, new UniversalMethodHandler(methodName));
        }
    }

    /**
     * Universal method handler that processes any gRPC method.
     */
    private class UniversalMethodHandler implements ServerCallHandler<String, String> {
        private final String fullMethodName;
        
        UniversalMethodHandler(String fullMethodName) {
            this.fullMethodName = fullMethodName;
        }
        
        @Override
        public ServerCall.Listener<String> startCall(ServerCall<String, String> call, Metadata headers) {
            return new UniversalCallListener(call, headers, fullMethodName);
        }
    }

    /**
     * Listener for gRPC calls that processes JSON payloads.
     */
    private class UniversalCallListener extends ServerCall.Listener<String> {
        private final ServerCall<String, String> call;
        private final Metadata headers;
        private final String fullMethodName;
        private String requestJson;

        UniversalCallListener(ServerCall<String, String> call, Metadata headers, String fullMethodName) {
            this.call = call;
            this.headers = headers;
            this.fullMethodName = fullMethodName;
        }

        @Override
        public void onMessage(String message) {
            this.requestJson = message;
            logger.debug("Received gRPC message for {}: {}", fullMethodName, message);
        }

        @Override
        public void onHalfClose() {
            if (rendezvous == null) {
                call.close(Status.UNAVAILABLE.withDescription("Service unavailable"), new Metadata());
                return;
            }

            try {
                // Extract metadata
                Map<String, String> metadataMap = extractMetadata(headers);
                
                // Create gRPC request
                GrpcRequest grpcRequest = GrpcRequest.fromFullMethodName(fullMethodName, requestJson);
                if (!metadataMap.isEmpty()) {
                    grpcRequest = GrpcRequest.withMetadata(
                        grpcRequest.getServiceName(),
                        grpcRequest.getMethodName(),
                        grpcRequest.getJsonPayload(),
                        metadataMap
                    );
                }

                // Process through Horizon
                HorizonContext context = rendezvous.encounter(grpcRequest);
                GrpcResponse grpcResponse = (GrpcResponse) rendezvous.fallAway(context);

                // Send response
                call.sendHeaders(new Metadata());
                
                if (grpcResponse.isSuccess()) {
                    call.sendMessage(grpcResponse.getJsonPayload());
                    call.close(Status.OK, new Metadata());
                } else {
                    Status grpcStatus = mapToGrpcStatus(grpcResponse.getStatus());
                    call.close(grpcStatus.withDescription(grpcResponse.getErrorMessage()), new Metadata());
                }

            } catch (Exception e) {
                logger.error("Error processing gRPC request: {}", fullMethodName, e);
                call.close(Status.INTERNAL.withDescription(e.getMessage()).withCause(e), new Metadata());
            }
        }

        @Override
        public void onCancel() {
            logger.debug("gRPC call cancelled: {}", fullMethodName);
        }

        @Override
        public void onComplete() {
            logger.debug("gRPC call completed: {}", fullMethodName);
        }
    }

    /**
     * Extracts metadata from gRPC headers.
     */
    private Map<String, String> extractMetadata(Metadata headers) {
        Map<String, String> metadataMap = new HashMap<>();
        
        for (String key : headers.keys()) {
            if (!key.endsWith("-bin")) {  // Skip binary headers
                Metadata.Key<String> metadataKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                String value = headers.get(metadataKey);
                if (value != null) {
                    metadataMap.put(key, value);
                }
            }
        }
        
        return metadataMap;
    }

    /**
     * Maps our simplified status to gRPC status.
     */
    private Status mapToGrpcStatus(GrpcResponse.Status status) {
        return switch (status) {
            case OK -> Status.OK;
            case CANCELLED -> Status.CANCELLED;
            case UNKNOWN -> Status.UNKNOWN;
            case INVALID_ARGUMENT -> Status.INVALID_ARGUMENT;
            case DEADLINE_EXCEEDED -> Status.DEADLINE_EXCEEDED;
            case NOT_FOUND -> Status.NOT_FOUND;
            case ALREADY_EXISTS -> Status.ALREADY_EXISTS;
            case PERMISSION_DENIED -> Status.PERMISSION_DENIED;
            case RESOURCE_EXHAUSTED -> Status.RESOURCE_EXHAUSTED;
            case FAILED_PRECONDITION -> Status.FAILED_PRECONDITION;
            case ABORTED -> Status.ABORTED;
            case OUT_OF_RANGE -> Status.OUT_OF_RANGE;
            case UNIMPLEMENTED -> Status.UNIMPLEMENTED;
            case INTERNAL -> Status.INTERNAL;
            case UNAVAILABLE -> Status.UNAVAILABLE;
        };
    }

    /**
     * Simple JSON string marshaller for gRPC.
     */
    private static class JsonStringMarshaller implements MethodDescriptor.Marshaller<String> {
        
        @Override
        public InputStream stream(String value) {
            if (value == null) {
                value = "{}";
            }
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public String parse(InputStream stream) {
            try {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("Failed to parse JSON string from gRPC stream", e);
                return "{}";
            }
        }
    }
}
