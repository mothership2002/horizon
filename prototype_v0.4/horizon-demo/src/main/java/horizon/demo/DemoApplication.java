package horizon.demo;

import horizon.core.ProtocolAggregator;
import horizon.web.http.HttpFoyer;
import horizon.web.http.HttpProtocol;
import horizon.web.websocket.WebSocketFoyer;
import horizon.web.websocket.WebSocketProtocol;
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
        logger.info("║  🔒 Protocol Access Control                              ║");
        logger.info("║  🌟 Automatic DTO Serialization                          ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║ HTTP Endpoints (port 8080):                              ║");
        logger.info("║                                                          ║");
        logger.info("║   GET  /                    → system.welcome             ║");
        logger.info("║   GET  /system/health       → system.health              ║");
        logger.info("║   GET  /system/info         → system.info                ║");
        logger.info("║                                                          ║");
        logger.info("║   POST /users               → user.create                ║");
        logger.info("║   GET  /users/{id}          → user.get                   ║");
        logger.info("║   PUT  /users/{id}          → user.update                ║");
        logger.info("║   DELETE /users/{id}        → user.delete                ║");
        logger.info("║   GET  /users               → user.list                  ║");
        logger.info("║   GET  /users/search        → user.search                ║");
        logger.info("║                                                          ║");
        logger.info("║   [HTTP ONLY] POST /users/bulk-create                    ║");
        logger.info("║   [HTTP ONLY] POST /users/import                         ║");
        logger.info("║   [HTTP ONLY] GET  /users/export                         ║");
        logger.info("║   [HTTP ONLY] POST /admin/shutdown                       ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║ WebSocket (port 8081):                                   ║");
        logger.info("║                                                          ║");
        logger.info("║   Connect to: ws://localhost:8081/ws                     ║");
        logger.info("║                                                          ║");
        logger.info("║   Allowed intents:                                       ║");
        logger.info("║   - user.create, user.get, user.update, user.delete      ║");
        logger.info("║   - user.list, user.search, user.validate                ║");
        logger.info("║   - chat.join, chat.message, chat.leave (WS ONLY)        ║");
        logger.info("║                                                          ║");
        logger.info("║   ❌ Blocked: admin.*, user.import, user.export          ║");
        logger.info("║                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════╣");
        logger.info("║                                                          ║");
        logger.info("║  The SAME @Conductor classes handle BOTH protocols! 🎉   ║");
        logger.info("║  With automatic DTO serialization! 🚀                    ║");
        logger.info("║                                                          ║");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }
}
