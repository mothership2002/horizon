package horizon.demo;

import horizon.core.ProtocolAggregator;
import horizon.http.HttpFoyer;
import horizon.http.HttpProtocol;
import horizon.websocket.WebSocketFoyer;
import horizon.websocket.WebSocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo application showing Annotation-based Conductors with Protocol Aggregation.
 * Clean, declarative, and powerful!
 */
public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Horizon Demo Application");
        
        // Create the Protocol Aggregator - the heart of Horizon
        ProtocolAggregator aggregator = new ProtocolAggregator();
        
        // Register protocols
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        
        // Scan and register conductors - So clean! So simple!
        aggregator.scanConductors("horizon.demo.conductors");
        
        // Start the aggregator
        aggregator.start();
        
        printStartupMessage();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down demo application");
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
        logger.info("╔══════════════════════════════════════════════════════════╗");
        logger.info("║        Horizon Framework v0.4 - Demo Application         ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║                                                          ║");
        logger.info("║  🌟 Annotation-based Conductors                          ║");
        logger.info("║  🌟 Protocol Aggregation                                 ║");
        logger.info("║  🌟 Write Once, Use Everywhere!                          ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║ HTTP Endpoints (port 8080):                              ║");
        logger.info("║                                                          ║");
        logger.info("║   GET  /                    → system.welcome             ║");
        logger.info("║   GET  /system/health       → system.health              ║");
        logger.info("║   GET  /system/info         → system.info                ║");
        logger.info("║                                                          ║");
        logger.info("║   POST /users/create        → user.create                ║");
        logger.info("║   GET  /users/{id}          → user.get                   ║");
        logger.info("║   PUT  /users/{id}          → user.update                ║");
        logger.info("║   DELETE /users/{id}        → user.delete                ║");
        logger.info("║   GET  /users/list          → user.list                  ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║ WebSocket (port 8081):                                   ║");
        logger.info("║                                                          ║");
        logger.info("║   Connect to: ws://localhost:8081/ws                     ║");
        logger.info("║                                                          ║");
        logger.info("║   Send: {\"intent\": \"user.create\",                       ║");
        logger.info("║          \"data\": {\"name\": \"...\", \"email\": \"...\"}}      ║");
        logger.info("║                                                          ║");
        logger.info("║   All the same intents work here too!                   ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║                                                          ║");
        logger.info("║  The SAME @Conductor classes handle BOTH protocols! 🎉   ║");
        logger.info("║                                                          ║");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }
}
