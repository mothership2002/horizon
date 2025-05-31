package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * User management conductor demonstrating protocol-neutral parameter handling.
 * This single implementation works seamlessly with HTTP, gRPC, and WebSocket.
 */
@Conductor(namespace = "user")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.GRPC, ProtocolNames.WEBSOCKET})
public class UserConductor {
    private static final Logger logger = LoggerFactory.getLogger(UserConductor.class);
    
    // Simple in-memory storage for demo
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    /**
     * Creates a new user.
     * Protocol-neutral parameters automatically work with:
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
        
        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, name, email, System.currentTimeMillis());
        users.put(userId, user);
        
        return Map.of(
            "userId", user.id(),
            "name", user.name(),
            "email", user.email(),
            "createdAt", user.createdAt(),
            "success", true
        );
    }
    
    /**
     * Gets a user by ID.
     * @Param automatically finds userId from:
     * - HTTP: /users/{userId} or ?userId=xxx
     * - gRPC: message.user_id or message.userId
     * - WebSocket: data.userId
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
                "message", "User not found: " + userId
            );
        }
        
        return Map.of(
            "found", true,
            "userId", user.id(),
            "name", user.name(),
            "email", user.email(),
            "createdAt", user.createdAt()
        );
    }
    
    /**
     * Updates a user.
     * Demonstrates optional parameters with protocol-neutral handling.
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
                "message", "User not found: " + userId
            );
        }
        
        // Update user with new values
        User updatedUser = new User(
            userId,
            name != null ? name : user.name(),
            email != null ? email : user.email(),
            user.createdAt()
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
     * Lists users with pagination and search.
     * Shows how query parameters work across all protocols.
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
        
        List<User> filteredUsers = users.values().stream()
            .filter(user -> search == null || 
                           user.name().toLowerCase().contains(search.toLowerCase()) ||
                           user.email().toLowerCase().contains(search.toLowerCase()))
            .sorted(Comparator.comparing(User::createdAt).reversed())
            .skip(offset)
            .limit(limit)
            .toList();
        
        List<Map<String, Object>> userList = filteredUsers.stream()
            .map(user -> Map.of(
                "userId", user.id(),
                "name", user.name(),
                "email", user.email(),
                "createdAt", user.createdAt()
            ))
            .toList();
        
        return Map.of(
            "users", userList,
            "total", users.size(),
            "limit", limit,
            "offset", offset,
            "hasMore", offset + filteredUsers.size() < users.size()
        );
    }
    
    /**
     * Deletes a user.
     * Demonstrates header parameter hints for auth tokens.
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
                "message", "User not found: " + userId
            );
        }
        
        return Map.of(
            "success", true,
            "message", "User deleted successfully",
            "deletedUser", Map.of(
                "userId", removed.id(),
                "name", removed.name()
            )
        );
    }
    
    /**
     * Validates user data.
     * Shows how to handle validation across protocols.
     */
    @Intent("validate")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users/validate"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ValidateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.validate")
        }
    )
    public Map<String, Object> validateUser(
        @Param(value = "name", required = false) String name,
        @Param(value = "email", required = false) String email
    ) {
        Map<String, String> errors = new HashMap<>();
        
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Name is required");
        } else if (name.length() < 2) {
            errors.put("name", "Name must be at least 2 characters");
        }
        
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Invalid email format");
        }
        
        return Map.of(
            "valid", errors.isEmpty(),
            "errors", errors
        );
    }
    
    /**
     * Simple user record for internal storage.
     */
    private record User(String id, String name, String email, long createdAt) {}
}
