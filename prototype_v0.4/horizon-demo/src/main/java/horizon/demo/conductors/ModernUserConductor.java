package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import horizon.demo.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Modern User management conductor using the new unified @ProtocolAccess annotation.
 * This demonstrates how to migrate from the old annotations to the new approach.
 */
@Conductor(namespace = "v2.user")
public class ModernUserConductor {
    private static final Logger logger = LoggerFactory.getLogger(ModernUserConductor.class);
    
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(2000);
    
    /**
     * Simple protocol access - both HTTP and WebSocket can access with convention-based routing.
     * HTTP: POST /v2/users
     * WebSocket: v2.user.create
     */
    @Intent("create")
    @ProtocolAccess({ProtocolNames.HTTP, ProtocolNames.WEBSOCKET})
    public User createUser(CreateUserRequest request) {
        logger.info("Creating user (v2): {}", request);
        
        validateUserRequest(request);
        
        Long id = idGenerator.incrementAndGet();
        User user = new User(id, request.getName(), request.getEmail());
        users.put(id, user);
        
        return user;
    }
    
    /**
     * Custom schema for each protocol.
     */
    @Intent("search")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /api/v2/users/search"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "v2.user.search")
        }
    )
    public SearchUserResponse searchUsers(SearchUserRequest request) {
        String query = request.getQ();
        String searchBy = request.getSearchBy() != null ? request.getSearchBy() : "name";
        
        logger.info("Searching users (v2) with query: {} by {}", query, searchBy);
        
        if (query == null || query.trim().isEmpty()) {
            return new SearchUserResponse(query, Collections.emptyList());
        }
        
        List<User> results = users.values().stream()
            .filter(user -> {
                String value = "name".equals(searchBy) ? user.getName() : user.getEmail();
                return value != null && value.toLowerCase().contains(query.toLowerCase());
            })
            .collect(Collectors.toList());
        
        return new SearchUserResponse(query, results);
    }
    
    /**
     * HTTP-only endpoint with custom attributes.
     */
    @Intent("export")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "GET /api/v2/users/export",
            attributes = {"contentType", "text/csv", "cache", "no-cache"}
        )
    )
    public ExportUserResponse exportUsers(ExportUserRequest request) {
        String format = request.getFormat() != null ? request.getFormat() : "csv";
        logger.info("Exporting users (v2) in format: {}", format);
        
        List<User> userList = new ArrayList<>(users.values());
        return new ExportUserResponse(format, userList);
    }
    
    /**
     * WebSocket-only streaming endpoint.
     */
    @Intent("subscribe")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.WEBSOCKET,
            value = "v2.user.updates",
            attributes = {"streaming", "true", "subscription", "true"}
        )
    )
    public Map<String, Object> subscribeToUserUpdates(Map<String, Object> payload) {
        String sessionId = (String) payload.get("_sessionId");
        logger.info("Session {} subscribing to user updates", sessionId);
        
        return Map.of(
            "subscribed", true,
            "channel", "v2.user.updates",
            "sessionId", sessionId
        );
    }
    
    /**
     * Multiple HTTP methods with the same WebSocket event.
     */
    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PUT /api/v2/users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PATCH /api/v2/users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "v2.user.update")
        }
    )
    public User updateUser(UpdateUserRequest request) {
        Long id = request.getId();
        logger.info("Updating user (v2) with id: {}", id);
        
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        
        if (request.getEmail() != null) {
            if (!request.getEmail().contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            user.setEmail(request.getEmail());
        }
        
        user.setUpdatedAt(System.currentTimeMillis());
        return user;
    }
    
    /**
     * RESTful resource with custom path.
     */
    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /api/v2/users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "v2.user.get")
        }
    )
    public User getUser(GetUserRequest request) {
        Long id = request.getId();
        User user = users.get(id);
        
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        
        return user;
    }
    
    /**
     * Batch operation - HTTP only.
     */
    @Intent("bulkCreate")
    @ProtocolAccess(
        schema = @ProtocolSchema(
            protocol = ProtocolNames.HTTP,
            value = "POST /api/v2/users/bulk",
            attributes = {"maxBatchSize", "100"}
        )
    )
    public BulkCreateUserResponse bulkCreateUsers(BulkCreateUserRequest request) {
        logger.info("Bulk creating {} users (v2)", request.getUsers().size());
        
        if (request.getUsers().size() > 100) {
            throw new IllegalArgumentException("Batch size exceeds maximum of 100");
        }
        
        List<User> createdUsers = new ArrayList<>();
        for (CreateUserRequest userRequest : request.getUsers()) {
            try {
                User user = createUser(userRequest);
                createdUsers.add(user);
            } catch (Exception e) {
                logger.error("Failed to create user in bulk: {}", userRequest, e);
            }
        }
        
        return new BulkCreateUserResponse(createdUsers);
    }
    
    private void validateUserRequest(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
    }
}
