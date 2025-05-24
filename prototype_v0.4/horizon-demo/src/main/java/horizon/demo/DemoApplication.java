package horizon.demo;

import horizon.core.AbstractConductor;
import horizon.core.ProtocolAggregator;
import horizon.http.HttpFoyer;
import horizon.http.HttpProtocol;
import horizon.websocket.WebSocketFoyer;
import horizon.websocket.WebSocketProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demo application showing Protocol Aggregation in action.
 * The SAME business logic works with BOTH HTTP and WebSocket!
 */
public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    // Simple in-memory storage for demo
    private static final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(1000);

    public static void main(String[] args) {
        logger.info("Starting Horizon Demo Application");

        // Create the Protocol Aggregator - the heart of Horizon
        ProtocolAggregator aggregator = new ProtocolAggregator();

        // Register HTTP protocol
        HttpProtocol httpProtocol = new HttpProtocol();
        HttpFoyer httpFoyer = new HttpFoyer(8080);
        aggregator.registerProtocol(httpProtocol, httpFoyer);

        // Register WebSocket protocol
        WebSocketProtocol wsProtocol = new WebSocketProtocol();
        WebSocketFoyer wsFoyer = new WebSocketFoyer(8081);
        aggregator.registerProtocol(wsProtocol, wsFoyer);

        // Register conductors for business logic
        // Notice: The SAME conductors work for BOTH protocols!

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

                // Create user
                Long id = idGenerator.incrementAndGet();
                Map<String, Object> user = new HashMap<>();
                user.put("id", id);
                user.put("name", name);
                user.put("email", email);
                user.put("createdAt", System.currentTimeMillis());

                // Store in memory
                users.put(id, user);

                logger.info("User created: {}", user);
                return user;
            }
        });

        // User retrieval conductor
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.get") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                Long id = extractId(payload);
                logger.info("Getting user with id: {}", id);

                if (id == null) {
                    throw new IllegalArgumentException("User ID is required");
                }

                Map<String, Object> user = users.get(id);
                if (user == null) {
                    throw new IllegalArgumentException("User not found: " + id);
                }

                return user;
            }

            private Long extractId(Map<String, Object> payload) {
                Object id = payload.get("id");
                if (id instanceof Number) {
                    return ((Number) id).longValue();
                } else if (id instanceof String) {
                    try {
                        return Long.parseLong((String) id);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // List all users conductor
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("user.list") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                logger.info("Listing all users");
                Map<String, Object> response = new HashMap<>();
                response.put("users", users.values());
                response.put("count", users.size());
                return response;
            }
        });

        // Welcome conductor (for root path)
        aggregator.registerConductor(new AbstractConductor<Map<String, Object>, Map<String, Object>>("welcome") {
            @Override
            public Map<String, Object> conduct(Map<String, Object> payload) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Welcome to Horizon Framework v0.4!");
                response.put("version", "0.4.0-SNAPSHOT");
                response.put("protocols", new String[]{"HTTP", "WebSocket"});
                response.put("timestamp", System.currentTimeMillis());
                return response;
            }
        });

        // Start the aggregator
        aggregator.start();

        logger.info("============================================");
        logger.info("Demo application started successfully!");
        logger.info("============================================");
        logger.info("Try these endpoints:");
        logger.info("");
        logger.info("HTTP (port 8080):");
        logger.info("  GET  http://localhost:8080/");
        logger.info("  POST http://localhost:8080/users/create");
        logger.info("       Body: {\"name\": \"John Doe\", \"email\": \"john@example.com\"}");
        logger.info("  GET  http://localhost:8080/users/1001");
        logger.info("  GET  http://localhost:8080/users/list");
        logger.info("");
        logger.info("WebSocket (port 8081):");
        logger.info("  Connect to: ws://localhost:8081/ws");
        logger.info("  Send: {\"intent\": \"user.create\", \"data\": {\"name\": \"Jane Doe\", \"email\": \"jane@example.com\"}}");
        logger.info("  Send: {\"intent\": \"user.get\", \"data\": {\"id\": 1001}}");
        logger.info("  Send: {\"intent\": \"user.list\", \"data\": {}}");
        logger.info("");
        logger.info("The SAME business logic handles BOTH protocols!");
        logger.info("============================================");

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