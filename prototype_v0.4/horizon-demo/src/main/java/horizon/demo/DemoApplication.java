package horizon.demo;

import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
import horizon.web.grpc.GrpcFoyer;
import horizon.web.grpc.GrpcProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Horizon Framework Demo Application.
 * Demonstrates protocol-neutral development with HTTP, WebSocket, and gRPC.
 */
public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Horizon Framework v0.4 Demo");

        // Create the Protocol Aggregator
        ProtocolAggregator aggregator = new ProtocolAggregator();

        // Register protocols
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        aggregator.registerProtocol(new GrpcProtocol(), new GrpcFoyer(9090));

        // Scan and register conductors
        aggregator.scanConductors("horizon.demo.conductor");

        // Start the aggregator
        aggregator.start();

        printStartupMessage();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Horizon Framework");
            aggregator.stop();
        }));

        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void printStartupMessage() {
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════╗
            ║        Horizon Framework v0.4 - Protocol Neutral         ║
            ╠══════════════════════════════════════════════════════════╣
            ║                                                          ║
            ║  🚀 Features:                                            ║
            ║  • Protocol-neutral @Param annotation                    ║
            ║  • Smart parameter resolution across all protocols       ║
            ║  • One business logic, multiple protocols                ║
            ║  • Automatic DTO conversion                              ║
            ║                                                          ║
            ╠══════════════════════════════════════════════════════════╣
            ║ HTTP (port 8080):                                        ║
            ║   POST   /users              → user.create               ║
            ║   GET    /users/{userId}     → user.get                  ║
            ║   PUT    /users/{userId}     → user.update               ║
            ║   DELETE /users/{userId}     → user.delete               ║
            ║   GET    /users              → user.list                 ║
            ║                                                          ║
            ║ WebSocket (port 8081):                                   ║
            ║   Connect: ws://localhost:8081/ws                        ║
            ║   Send: {"intent": "user.create", "data": {...}}         ║
            ║                                                          ║
            ║ gRPC (port 9090):                                        ║
            ║   UserService/CreateUser     → user.create               ║
            ║   UserService/GetUser        → user.get                  ║
            ║   UserService/UpdateUser     → user.update               ║
            ║   UserService/DeleteUser     → user.delete               ║
            ║   UserService/ListUsers      → user.list                 ║
            ║                                                          ║
            ╠══════════════════════════════════════════════════════════╣
            ║  The SAME @Conductor handles ALL protocols! 🎉           ║
            ╚══════════════════════════════════════════════════════════╝
            
            Ready to accept requests...
            """);
    }
}
