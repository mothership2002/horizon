package horizon.web.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import horizon.core.HorizonContext;
import horizon.web.common.AbstractFoyer;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * gRPC Foyer - the entry point for gRPC requests into the Horizon framework.
 * Creates a gRPC server that delegates to the Horizon processing pipeline.
 */
public class GrpcFoyer extends AbstractFoyer<GrpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(GrpcFoyer.class);

    private Server grpcServer;
    private final GrpcConfiguration configuration;
    private final GrpcMessageConverter messageConverter = new GrpcMessageConverter();

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
                ServerBuilder<?> serverBuilder;

                // Check if TLS is enabled
                if (configuration.isTlsEnabled()) {
                    if (configuration.getCertChainFile() == null || configuration.getPrivateKeyFile() == null) {
                        throw new IllegalStateException("TLS is enabled but certificate or private key file is missing");
                    }

                    logger.info("Configuring gRPC server with TLS");
                    serverBuilder = ServerBuilder.forPort(port)
                        .useTransportSecurity(
                            configuration.getCertChainFile(),
                            configuration.getPrivateKeyFile()
                        );
                } else {
                    logger.info("Configuring gRPC server without TLS (plaintext)");
                    serverBuilder = ServerBuilder.forPort(port);
                }

                // Apply configuration
                serverBuilder.maxInboundMessageSize(configuration.getMaxInboundMessageSize());
                serverBuilder.maxInboundMetadataSize(configuration.getMaxInboundMetadataSize());

                // Add interceptors
                for (ServerInterceptor interceptor : configuration.getInterceptors()) {
                    serverBuilder.intercept(interceptor);
                }

                // Add the dynamic Horizon service with generic handler
                serverBuilder.fallbackHandlerRegistry(new HorizonHandlerRegistry());

                // Add any additional configured services
                for (BindableService service : configuration.getServices()) {
                    serverBuilder.addService(service);
                }

                grpcServer = serverBuilder.build().start();

                logger.info("gRPC Foyer opened successfully on port {}", port);

                // Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down gRPC server");
                    GrpcFoyer.this.close();
                }));

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
     * Handler registry that handles all gRPC methods dynamically.
     */
    private class HorizonHandlerRegistry extends HandlerRegistry {

        @Override
        public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
            logger.debug("Looking up method: {} for authority: {}", methodName, authority);

            // Create a generic method definition for any method
            MethodDescriptor<ByteString, ByteString> methodDescriptor = MethodDescriptor.<ByteString, ByteString>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(methodName)
                .setRequestMarshaller(new ByteStringMarshaller())
                .setResponseMarshaller(new ByteStringMarshaller())
                .build();

            return ServerMethodDefinition.create(methodDescriptor, new GenericUnaryHandler(methodName));
        }
    }

    /**
     * Generic handler for unary calls.
     */
    private class GenericUnaryHandler implements ServerCallHandler<ByteString, ByteString> {
        private final String fullMethodName;

        GenericUnaryHandler(String fullMethodName) {
            this.fullMethodName = fullMethodName;
        }

        @Override
        public ServerCall.Listener<ByteString> startCall(ServerCall<ByteString, ByteString> call, Metadata headers) {
            return new UnaryServerCallListener(call, headers, fullMethodName);
        }
    }

    /**
     * Listener for unary gRPC calls.
     */
    private class UnaryServerCallListener extends ServerCall.Listener<ByteString> {
        private final ServerCall<ByteString, ByteString> call;
        private final Metadata headers;
        private final String fullMethodName;
        private ByteString requestBytes;

        UnaryServerCallListener(ServerCall<ByteString, ByteString> call, Metadata headers, String fullMethodName) {
            this.call = call;
            this.headers = headers;
            this.fullMethodName = fullMethodName;
        }

        @Override
        public void onMessage(ByteString message) {
            this.requestBytes = message;
        }

        @Override
        public void onHalfClose() {
            if (rendezvous == null) {
                call.close(Status.UNAVAILABLE.withDescription("Service unavailable"), new Metadata());
                return;
            }

            try {
                // Parse service and method names
                String[] parts = fullMethodName.split("/");
                String serviceName = parts.length > 1 ? parts[0] : "Unknown";
                String methodName = parts.length > 1 ? parts[1] : fullMethodName;

                // Try to get message types from registry
                GrpcServiceRegistry.MessageTypePair messageTypes = 
                    GrpcServiceRegistry.getInstance().getMessageTypes(fullMethodName);

                Message requestMessage = null;
                if (messageTypes != null && messageTypes.hasRequestType()) {
                    try {
                        requestMessage = messageConverter.bytesToMessage(requestBytes, messageTypes.requestType());
                    } catch (Exception e) {
                        logger.warn("Failed to parse request as Protocol Buffer, using raw bytes", e);
                    }
                }

                // Create GrpcRequest
                GrpcRequest grpcRequest = new GrpcRequest(
                    serviceName,
                    methodName,
                    requestMessage,
                    headers,
                    call.getMethodDescriptor()
                );

                // Add raw bytes for fallback processing
                grpcRequest.setRawRequestBytes(requestBytes);

                // Process through Horizon
                HorizonContext context = rendezvous.encounter(grpcRequest);
                GrpcResponse grpcResponse = (GrpcResponse) rendezvous.fallAway(context);

                // Send response
                call.sendHeaders(new Metadata());

                if (grpcResponse.isSuccess()) {
                    ByteString responseBytes;

                    if (grpcResponse.getMessage() != null) {
                        // Convert Protocol Buffer message to bytes
                        responseBytes = messageConverter.messageToBytes(grpcResponse.getMessage());
                    } else if (grpcResponse.getRawResponseBytes() != null) {
                        // Use raw bytes if provided
                        responseBytes = grpcResponse.getRawResponseBytes();
                    } else {
                        // Empty response
                        responseBytes = ByteString.EMPTY;
                    }

                    call.sendMessage(responseBytes);
                    call.close(Status.OK, grpcResponse.getTrailers());
                } else {
                    call.close(grpcResponse.getStatus(), grpcResponse.getTrailers());
                }

            } catch (Exception e) {
                logger.error("Error processing gRPC request", e);
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
     * Simple ByteString marshaller for generic message handling.
     */
    private static class ByteStringMarshaller implements MethodDescriptor.Marshaller<ByteString> {

        @Override
        public InputStream stream(ByteString value) {
            return value.newInput();
        }

        @Override
        public ByteString parse(InputStream stream) {
            try {
                return ByteString.readFrom(stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse ByteString", e);
            }
        }
    }
}
