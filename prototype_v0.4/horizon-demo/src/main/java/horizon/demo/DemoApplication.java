package horizon.demo;

import horizon.core.AbstractConductor;
import horizon.core.ProtocolAggregator;
import horizon.http.HttpFoyer;
import horizon.http.HttpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Demo application showing Protocol Aggregation in action.
 * A simple user management system accessible via HTTP (and easily extendable to WebSocket, gRPC, etc.)
 */
public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Horizon Demo Application");
        
        // Create the Protocol Aggregator - the heart of Horizon
        ProtocolAggregator aggregator = new ProtocolAggregator();
        
        // Register HTTP protocol
        HttpProtocol httpProtocol = new HttpProtocol();
        HttpFoyer httpFoyer = new HttpFoyer(8080);
        aggregator.registerProtocol(httpProtocol, httpFoyer);
        
        // Register conductors for business logic
        
        // User creation conductor
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.create") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                logger.info("Creating user with data: {}", payload);
                
                // Simple validation
                String name = (String) payload.get("name");
                String email = (String) payload.get("email");
                
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Name is required");
                }
                if (email == null || !email.contains("@")) {
                    throw new IllegalArgumentException("Valid email is required");
                }
                
                // Simulate user creation
                Map<String, Object> user = new HashMap<>();
                user.put("id", System.currentTimeMillis());
                user.put("name", name);
                user.put("email", email);
                user.put("createdAt", System.currentTimeMillis());
                
                return user;
            }
        });
        
        // User retrieval conductor
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.get") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                Long id = (Long) payload.get("id");
                logger.info("Getting user with id: {}", id);
                
                if (id == null) {
                    throw new IllegalArgumentException("User ID is required");
                }
                
                // Simulate user retrieval
                Map<String, Object> user = new HashMap<>();
                user.put("id", id);
                user.put("name", "John Doe");
                user.put("email", "john@example.com");
                
                return user;
            }
        });
        
        // Welcome conductor (for root path)
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("welcome") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Welcome to Horizon Framework v0.4!");
                response.put("version", "0.4.0-SNAPSHOT");
                response.put("timestamp", System.currentTimeMillis());
                return response;
            }
        });
        
        // Start the aggregator
        aggregator.start();
        
        logger.info("Demo application started successfully!");
        logger.info("Try these HTTP endpoints:");
        logger.info("  GET  http://localhost:8080/");
        logger.info("  POST http://localhost:8080/users/create");
        logger.info("  GET  http://localhost:8080/users/123");
        
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
}
