package horizon.web.grpc;

import horizon.web.common.AbstractWebFoyer;
import io.grpc.*;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * gRPC Foyer - the entry point for gRPC requests into the Horizon framework.
 * Creates a gRPC server that delegates to the Horizon processing pipeline.
 */
public class GrpcFoyer extends AbstractWebFoyer<GrpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(GrpcFoyer.class);

    private Server grpcServer;
    private final ServerServiceDefinition serviceDefinition;

    public GrpcFoyer(int port) {
        super(port);
        this.serviceDefinition = createServiceDefinition();
    }

    @Override
    protected String getProtocolName() {
        return "gRPC";
    }

    @Override
    protected io.netty.channel.ChannelInitializer<?> createChannelInitializer() {
        // gRPC uses its own Netty configuration
        return null;
    }

    @Override
    public void open() {
        if (isOpen.compareAndSet(false, true)) {
            logger.info("Opening gRPC Foyer on port {}", port);

            try {
                grpcServer = ServerBuilder.forPort(port)
                    .addService(new HorizonGrpcService())
                    .build()
                    .start();

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
                    grpcServer.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    grpcServer.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }

            logger.info("gRPC Foyer closed");
        }
    }

    /**
     * Creates a generic service definition that handles all gRPC calls.
     */
    private ServerServiceDefinition createServiceDefinition() {
        return ServerServiceDefinition.builder("horizon.HorizonService")
            .addMethod(
                createGenericMethodDescriptor("GenericCall"),
                createGenericHandler()
            )
            .build();
    }

    /**
     * Creates a generic method descriptor.
     */
    private <ReqT, RespT> MethodDescriptor<ReqT, RespT> createGenericMethodDescriptor(String methodName) {
        // This is a simplified version. In practice, you would need proper marshallers
        return MethodDescriptor.<ReqT, RespT>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName("horizon.HorizonService/" + methodName)
            .setRequestMarshaller((MethodDescriptor.Marshaller<ReqT>) createGenericMarshaller())
            .setResponseMarshaller((MethodDescriptor.Marshaller<RespT>) createGenericMarshaller())
            .build();
    }

    /**
     * Creates a generic marshaller.
     */
    private <T> MethodDescriptor.Marshaller<T> createGenericMarshaller() {
        return new MethodDescriptor.Marshaller<T>() {
            @Override
            public java.io.InputStream stream(T value) {
                // Implementation would serialize the message
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public T parse(java.io.InputStream stream) {
                // Implementation would deserialize the message
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

    /**
     * Creates a generic handler that delegates to Horizon.
     */
    private <ReqT, RespT> ServerCallHandler<ReqT, RespT> createGenericHandler() {
        return ServerCalls.asyncUnaryCall(
                this::handleRequest
        );
    }

    /**
     * Handles a gRPC request by delegating to the Horizon rendezvous.
     */
    private <ReqT, RespT> void handleRequest(ReqT request, StreamObserver<RespT> responseObserver) {
        if (rendezvous == null) {
            responseObserver.onError(
                Status.UNAVAILABLE.withDescription("Service unavailable").asException()
            );
            return;
        }

        try {
            // This is where you would convert the generic request to GrpcRequest
            // For now, this is a placeholder
            logger.warn("Generic gRPC handling not fully implemented");

            responseObserver.onError(
                Status.UNIMPLEMENTED.withDescription("Generic gRPC handling not implemented").asException()
            );

        } catch (Exception e) {
            logger.error("Error handling gRPC request", e);
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asException()
            );
        }
    }

    /**
     * Dynamic gRPC service that handles all incoming calls.
     */
    private static class HorizonGrpcService implements BindableService {
        @Override
        public ServerServiceDefinition bindService() {
            ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(getServiceDescriptor());

            // This is where you would dynamically add methods based on registered conductors
            // For now, we'll add a generic handler
            logger.info("Binding gRPC service with dynamic method handlers");

            return builder.build();
        }

        /**
         * Creates a service descriptor.
         */
        private ServiceDescriptor getServiceDescriptor() {
            return ServiceDescriptor.newBuilder("horizon.DynamicService")
                .build();
        }
    }
}
