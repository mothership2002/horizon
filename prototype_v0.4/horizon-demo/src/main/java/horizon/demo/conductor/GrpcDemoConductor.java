package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo conductor showing protocol-neutral parameter handling.
 * This conductor works seamlessly with HTTP, gRPC, and WebSocket.
 */
@Conductor(namespace = "user")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.GRPC, ProtocolNames.WEBSOCKET})
public class GrpcDemoConductor {
    private static final Logger logger = LoggerFactory.getLogger(GrpcDemoConductor.class);
    
    // Simple in-memory storage for demo
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    /**
     * Creates a new user.
     * Works with all protocols:
     * - HTTP: POST /users with JSON body
     * - gRPC: UserService/CreateUser with message
     * - WebSocket: {intent: "user.create", data: {...}}
     */
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.create")
        }
    )
    public Map<String, Object> createUser(
        @Param("name") String name,
        @Param("email") String email
    ) {
        logger.info("Creating user: {} ({})", name, email);
        
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, name, email);
        users.put(userId, user);
        
        return Map.of(
            "userId", userId,
            "success", true,
            "message", "User created successfully"
        );
    }
    
    /**
     * Gets a user by ID.
     * Protocol-neutral parameter handling:
     * - HTTP: GET /users/{userId} or /users?userId=123
     * - gRPC: GetUserRequest { user_id: "123" }
     * - WebSocket: {intent: "user.get", data: {userId: "123"}}
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/GetUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.get")
        }
    )
    public Map<String, Object> getUser(@Param("userId") String userId) {
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
     * Updates a user.
     * Demonstrates mixed parameter sources.
     */
    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "PUT /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/UpdateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.update")
        }
    )
    public Map<String, Object> updateUser(
        @Param("userId") String userId,
        @Param(value = "name", required = false) String name,
        @Param(value = "email", required = false) String email
    ) {
        logger.info("Updating user: {}", userId);
        
        User user = users.get(userId);
        if (user == null) {
            return Map.of(
                "success", false,
                "message", "User not found"
            );
        }
        
        // Update user
        User updatedUser = new User(
            userId,
            name != null ? name : user.name(),
            email != null ? email : user.email()
        );
        users.put(userId, updatedUser);
        
        return Map.of(
            "success", true,
            "message", "User updated successfully",
            "user", Map.of(
                "userId", updatedUser.id(),
                "name", updatedUser.name(),
                "email", updatedUser.email()
            )
        );
    }
    
    /**
     * Lists all users with optional filtering.
     * Shows how query parameters work across protocols.
     */
    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ListUsers"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.list")  
        }
    )
    public Map<String, Object> listUsers(
        @Param(value = "limit", defaultValue = "10") int limit,
        @Param(value = "offset", defaultValue = "0") int offset,
        @Param(value = "search", required = false) String search
    ) {
        logger.info("Listing users - limit: {}, offset: {}, search: {}", limit, offset, search);
        
        var userList = users.values().stream()
            .filter(user -> search == null || 
                           user.name().toLowerCase().contains(search.toLowerCase()) ||
                           user.email().toLowerCase().contains(search.toLowerCase()))
            .skip(offset)
            .limit(limit)
            .map(user -> Map.of(
                "id", user.id(),
                "name", user.name(),
                "email", user.email()
            ))
            .toList();
        
        return Map.of(
            "users", userList,
            "total", users.size(),
            "limit", limit,
            "offset", offset,
            "hasMore", offset + userList.size() < users.size()
        );
    }
    
    /**
     * Deletes a user.
     * Demonstrates header parameter handling across protocols.
     */
    @Intent("delete")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "DELETE /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/DeleteUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.delete")
        }
    )
    public Map<String, Object> deleteUser(
        @Param("userId") String userId,
        @Param(value = "authToken", hints = {"header"}, required = false) String authToken
    ) {
        logger.info("Deleting user: {} (auth: {})", userId, authToken != null ? "provided" : "none");
        
        // In real app, validate authToken
        if (authToken == null || authToken.isEmpty()) {
            logger.warn("No auth token provided for delete operation");
        }
        
        User removed = users.remove(userId);
        if (removed == null) {
            return Map.of(
                "success", false,
                "message", "User not found"
            );
        }
        
        return Map.of(
            "success", true,
            "message", "User deleted successfully",
            "deletedUser", Map.of(
                "id", removed.id(),
                "name", removed.name()
            )
        );
    }
    
    /**
     * Simple user record for internal storage.
     */
    private record User(String id, String name, String email) {}
}
