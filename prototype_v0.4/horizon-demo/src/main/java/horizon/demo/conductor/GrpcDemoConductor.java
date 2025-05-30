package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo conductor showing gRPC integration with Horizon Framework.
 * This conductor can handle both HTTP and gRPC requests with the same business logic.
 */
@Conductor(namespace = "user")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.GRPC})
public class GrpcDemoConductor {
    private static final Logger logger = LoggerFactory.getLogger(GrpcDemoConductor.class);
    
    // Simple in-memory storage for demo
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    /**
     * Creates a new user.
     * Accessible via:
     * - HTTP: POST /users
     * - gRPC: UserService/CreateUser
     */
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUser")
        }
    )
    public Map<String, Object> createUser(@RequestBody Map<String, String> request) {
        logger.info("Creating user: {}", request.get("name"));
        
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, request.get("name"), request.get("email"));
        users.put(userId, user);
        
        return Map.of(
            "userId", userId,
            "success", true,
            "message", "User created successfully"
        );
    }
    
    /**
     * Gets a user by ID.
     * Accessible via:
     * - HTTP: GET /users/{id}
     * - gRPC: UserService/GetUser
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users/{id}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/GetUser")
        }
    )
    public Map<String, Object> getUser(@PathParam("id") String userId) {
        logger.info("Getting user: {}", userId);
        
        User user = users.get(userId);
        if (user == null) {
            return Map.of(
                "found", false,
                "message", "User not found"
            );
        }
        
        return Map.of(
            "found", true,
            "userId", user.id(),
            "name", user.name(),
            "email", user.email()
        );
    }
    
    /**
     * Lists all users.
     * Accessible via:
     * - HTTP: GET /users
     * - gRPC: UserService/ListUsers
     */
    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ListUsers")
        }
    )
    public Map<String, Object> listUsers(@QueryParam(value = "limit", defaultValue = "10") int limit) {
        logger.info("Listing users with limit: {}", limit);
        
        return Map.of(
            "users", users.values().stream()
                .limit(limit)
                .map(user -> Map.of(
                    "id", user.id(),
                    "name", user.name(),
                    "email", user.email()
                ))
                .toList(),
            "total", users.size(),
            "limit", limit
        );
    }
    
    /**
     * Simple user record for internal storage.
     */
    private record User(String id, String name, String email) {}
}
