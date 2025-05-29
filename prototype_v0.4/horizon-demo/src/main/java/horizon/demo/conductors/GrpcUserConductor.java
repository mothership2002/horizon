package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User conductor with gRPC support.
 * Demonstrates how the same business logic can be exposed via HTTP, WebSocket, and gRPC.
 */
@Conductor(namespace = "user")
public class GrpcUserConductor {
    private static final Logger logger = LoggerFactory.getLogger(GrpcUserConductor.class);
    
    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(3000);
    
    /**
     * Creates a new user.
     * Accessible via:
     * - HTTP: POST /api/users
     * - WebSocket: user.create
     * - gRPC: UserService/CreateUser
     */
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /api/users"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.create"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.CreateUser")
        }
    )
    public Map<String, Object> createUser(@RequestBody Map<String, Object> request) {
        logger.info("Creating user via gRPC-compatible conductor: {}", request);
        
        String name = (String) request.get("name");
        String email = (String) request.get("email");
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        Long id = idGenerator.incrementAndGet();
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());
        
        users.put(id, user);
        
        logger.info("User created with ID: {}", id);
        return user;
    }
    
    /**
     * Gets a user by ID.
     * Accessible via:
     * - HTTP: GET /api/users/{userId}
     * - WebSocket: user.get
     * - gRPC: UserService/GetUser
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /api/users/{userId}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.get"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.GetUser")
        }
    )
    public Map<String, Object> getUser(@PathParam("userId") Long userId) {
        logger.info("Getting user by ID: {}", userId);
        
        Map<String, Object> user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        return user;
    }
    
    /**
     * Updates a user.
     * Accessible via:
     * - HTTP: PUT /api/users/{userId}
     * - WebSocket: user.update
     * - gRPC: UserService/UpdateUser
     */
    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PUT /api/users/{userId}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.update"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.UpdateUser")
        }
    )
    public Map<String, Object> updateUser(
            @PathParam("userId") Long userId,
            @RequestBody Map<String, Object> updates
    ) {
        logger.info("Updating user {}: {}", userId, updates);
        
        Map<String, Object> user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Update fields
        if (updates.containsKey("name")) {
            user.put("name", updates.get("name"));
        }
        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }
            user.put("email", email);
        }
        
        user.put("updatedAt", System.currentTimeMillis());
        
        return user;
    }
    
    /**
     * Lists all users with pagination.
     * Accessible via:
     * - HTTP: GET /api/users?page=1&size=10
     * - WebSocket: user.list
     * - gRPC: UserService/ListUsers
     */
    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /api/users"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.list"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.ListUsers")
        }
    )
    public Map<String, Object> listUsers(
            @QueryParam(value = "page", defaultValue = "1") Integer page,
            @QueryParam(value = "size", defaultValue = "10") Integer size
    ) {
        logger.info("Listing users - page: {}, size: {}", page, size);
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users.values());
        response.put("page", page);
        response.put("size", size);
        response.put("totalCount", users.size());
        
        return response;
    }
    
    /**
     * Deletes a user.
     * Accessible via:
     * - HTTP: DELETE /api/users/{userId}
     * - WebSocket: user.delete
     * - gRPC: UserService/DeleteUser
     */
    @Intent("delete")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "DELETE /api/users/{userId}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.delete"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.DeleteUser")
        }
    )
    public Map<String, Object> deleteUser(@PathParam("userId") Long userId) {
        logger.info("Deleting user: {}", userId);
        
        Map<String, Object> user = users.remove(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", true);
        response.put("user", user);
        
        return response;
    }
    
    /**
     * Searches users.
     * This demonstrates a more complex gRPC method with multiple parameters.
     */
    @Intent("search")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /api/users/search"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.search"),
            @ProtocolSchema(protocol = ProtocolNames.GRPC, value = "UserService.SearchUsers")
        }
    )
    public Map<String, Object> searchUsers(@RequestBody Map<String, Object> searchRequest) {
        String query = (String) searchRequest.get("query");
        String field = (String) searchRequest.getOrDefault("field", "name");
        Integer maxResults = (Integer) searchRequest.getOrDefault("maxResults", 10);
        
        logger.info("Searching users - query: {}, field: {}, maxResults: {}", 
                   query, field, maxResults);
        
        // Simple search implementation
        var results = users.values().stream()
            .filter(user -> {
                String value = String.valueOf(user.get(field));
                return value.toLowerCase().contains(query.toLowerCase());
            })
            .limit(maxResults)
            .toList();
        
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("count", results.size());
        response.put("query", query);
        
        return response;
    }
}
