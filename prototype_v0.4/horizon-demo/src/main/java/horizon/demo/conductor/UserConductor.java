package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import horizon.demo.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * User management conductor that works seamlessly with HTTP, WebSocket, and gRPC.
 * Uses protocol-neutral @Param annotation for true multi-protocol support.
 * 
 * This conductor demonstrates how the same business logic can be exposed through
 * multiple protocols without code duplication.
 */
@Conductor(namespace = "user")
@ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.GRPC, ProtocolNames.WEBSOCKET})
public class UserConductor {
    private static final Logger logger = LoggerFactory.getLogger(UserConductor.class);
    
    // Simple in-memory storage for demo
    private final Map<String, UserData> users = new ConcurrentHashMap<>();
    private long idCounter = 1000;
    
    /**
     * Creates a new user.
     * Protocol mappings:
     * - HTTP: POST /users
     * - gRPC: UserService/CreateUser
     * - WebSocket: user.create
     */
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.create")
        }
    )
    @GrpcMethod(
        requestType = CreateUserRequest.class,
        responseType = CreateUserResponse.class
    )
    public Map<String, Object> createUser(
        @Param("name") String name,
        @Param("email") String email
    ) {
        logger.info("Creating user: name={}, email={}", name, email);
        
        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        // Create user
        String userId = String.valueOf(idCounter++);
        UserData user = new UserData(userId, name, email, System.currentTimeMillis());
        users.put(userId, user);
        
        // Return response compatible with all protocols
        Map<String, Object> response = new HashMap<>();
        response.put("user_id", userId);
        response.put("userId", userId); // Support both naming conventions
        response.put("success", true);
        response.put("message", "User created successfully");
        
        return response;
    }
    
    /**
     * Gets a user by ID.
     * Protocol mappings:
     * - HTTP: GET /users/{userId}
     * - gRPC: UserService/GetUser
     * - WebSocket: user.get
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/GetUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.get")
        }
    )
    @GrpcMethod(
        requestType = GetUserRequest.class,
        responseType = GetUserResponse.class
    )
    public Map<String, Object> getUser(
        @Param("userId") String userId,
        @Param(value = "user_id", required = false) String grpcUserId // Support gRPC snake_case
    ) {
        // Support both parameter names
        String id = userId != null ? userId : grpcUserId;
        logger.info("Getting user: {}", id);
        
        UserData user = users.get(id);
        
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("found", true);
            response.put("user_id", user.id);
            response.put("userId", user.id); // Both conventions
            response.put("name", user.name);
            response.put("email", user.email);
        } else {
            response.put("found", false);
            response.put("message", "User not found: " + id);
        }
        
        return response;
    }
    
    /**
     * Updates a user.
     * Protocol mappings:
     * - HTTP: PUT /users/{userId}
     * - gRPC: UserService/UpdateUser
     * - WebSocket: user.update
     */
    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "PUT /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/UpdateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.update")
        }
    )
    @GrpcMethod(
        requestType = UpdateUserRequest.class,
        responseType = UpdateUserResponse.class
    )
    public Map<String, Object> updateUser(
        @Param("userId") String userId,
        @Param(value = "user_id", required = false) String grpcUserId,
        @Param(value = "name", required = false) String name,
        @Param(value = "email", required = false) String email
    ) {
        String id = userId != null ? userId : grpcUserId;
        logger.info("Updating user {}: name={}, email={}", id, name, email);
        
        UserData user = users.get(id);
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found: " + id);
            return response;
        }
        
        // Update fields if provided
        if (name != null && !name.trim().isEmpty()) {
            user.name = name;
        }
        if (email != null && email.contains("@")) {
            user.email = email;
        }
        user.updatedAt = System.currentTimeMillis();
        
        // Return updated user info
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User updated successfully");
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.id);
        userInfo.put("name", user.name);
        userInfo.put("email", user.email);
        response.put("user", userInfo);
        
        return response;
    }
    
    /**
     * Deletes a user.
     * Protocol mappings:
     * - HTTP: DELETE /users/{userId}
     * - gRPC: UserService/DeleteUser
     * - WebSocket: user.delete
     */
    @Intent("delete")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "DELETE /users/{userId}"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/DeleteUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.delete")
        }
    )
    @GrpcMethod(
        requestType = DeleteUserRequest.class,
        responseType = DeleteUserResponse.class
    )
    public Map<String, Object> deleteUser(
        @Param("userId") String userId,
        @Param(value = "user_id", required = false) String grpcUserId,
        @Param(value = "authToken", hints = {"header"}, required = false) String authToken,
        @Param(value = "auth_token", required = false) String grpcAuthToken
    ) {
        String id = userId != null ? userId : grpcUserId;
        String token = authToken != null ? authToken : grpcAuthToken;
        
        logger.info("Deleting user: {} (auth: {})", id, token != null ? "provided" : "none");
        
        UserData removed = users.remove(id);
        
        Map<String, Object> response = new HashMap<>();
        if (removed != null) {
            response.put("success", true);
            response.put("message", "User deleted successfully");
            
            Map<String, Object> deletedUser = new HashMap<>();
            deletedUser.put("id", removed.id);
            deletedUser.put("name", removed.name);
            deletedUser.put("email", removed.email);
            response.put("deleted_user", deletedUser);
            response.put("deletedUser", deletedUser); // Both conventions
        } else {
            response.put("success", false);
            response.put("message", "User not found: " + id);
        }
        
        return response;
    }
    
    /**
     * Lists users with pagination.
     * Protocol mappings:
     * - HTTP: GET /users
     * - gRPC: UserService/ListUsers
     * - WebSocket: user.list
     */
    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "GET /users"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ListUsers"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.list")
        }
    )
    @GrpcMethod(
        requestType = ListUsersRequest.class,
        responseType = ListUsersResponse.class
    )
    public Map<String, Object> listUsers(
        @Param(value = "limit", defaultValue = "10") int limit,
        @Param(value = "offset", defaultValue = "0") int offset
    ) {
        logger.info("Listing users - limit: {}, offset: {}", limit, offset);
        
        List<Map<String, Object>> userList = users.values().stream()
            .sorted(Comparator.comparing(u -> u.createdAt))
            .skip(offset)
            .limit(limit)
            .map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.id);
                userInfo.put("name", user.name);
                userInfo.put("email", user.email);
                return userInfo;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("total", users.size());
        response.put("limit", limit);
        response.put("offset", offset);
        response.put("hasMore", offset + userList.size() < users.size());
        
        return response;
    }
    
    /**
     * Validates user data.
     * Protocol mappings:
     * - HTTP: POST /users/validate
     * - gRPC: UserService/ValidateUser
     * - WebSocket: user.validate
     */
    @Intent("validate")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users/validate"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService/ValidateUser"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.validate")
        }
    )
    @GrpcMethod(
        requestType = ValidateUserRequest.class,
        responseType = ValidateUserResponse.class
    )
    public Map<String, Object> validateUser(
        @Param(value = "name", required = false) String name,
        @Param(value = "email", required = false) String email
    ) {
        logger.info("Validating user data: name={}, email={}", name, email);
        
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", errors.isEmpty());
        response.put("errors", errors);
        
        return response;
    }
    
    /**
     * Simple user data holder.
     */
    private static class UserData {
        String id;
        String name;
        String email;
        long createdAt;
        long updatedAt;
        
        UserData(String id, String name, String email, long createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.createdAt = createdAt;
            this.updatedAt = createdAt;
        }
    }
}
