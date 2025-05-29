package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import horizon.demo.dto.user.User;
import horizon.demo.dto.user.request.*;
import horizon.demo.dto.user.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * User management conductor using the new @ProtocolAccess annotation.
 */
@Conductor(namespace = "user")
public class UserConductorNew {
    private static final Logger logger = LoggerFactory.getLogger(UserConductorNew.class);

    // Simple in-memory storage for demo
    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);

    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.create")
        }
    )
    public User createUser(@RequestBody CreateUserRequest request) {
        logger.info("Creating user with data: {}", request);

        // Extract and validate
        String name = request.getName();
        String email = request.getEmail();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }

        // Create user
        Long id = idGenerator.incrementAndGet();
        User user = new User(id, name, email);

        // Store
        users.put(id, user.toMap());

        logger.info("User created: {}", user);
        return user;
    }

    @Intent("bulkCreate")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users/bulk-create")
    )
    public BulkCreateUserResponse bulkCreateUsers(@RequestBody BulkCreateUserRequest request) {
        logger.info("Bulk creating users");

        List<CreateUserRequest> userRequests = request.getUsers();

        if (userRequests == null || userRequests.isEmpty()) {
            throw new IllegalArgumentException("Users list is required");
        }

        List<User> createdUsers = new ArrayList<>();
        for (CreateUserRequest userRequest : userRequests) {
            try {
                User created = createUser(userRequest);
                createdUsers.add(created);
            } catch (Exception e) {
                logger.error("Failed to create user: {}", userRequest, e);
                // Continue with other users
            }
        }

        return new BulkCreateUserResponse(createdUsers);
    }

    @Intent("import")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users/import")
    )
    public Map<String, Object> importUsers(@RequestBody Map<String, Object> payload) {
        logger.info("Importing users from external source");

        String source = (String) payload.get("source");
        String format = (String) payload.get("format");

        // Simulate import logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Import started");
        response.put("source", source);
        response.put("format", format);
        response.put("status", "processing");

        return response;
    }

    @Intent("search")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users/search"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.search")
        }
    )
    public SearchUserResponse searchUsers(SearchUserRequest request) {
        String query = request.getQ();
        String searchBy = request.getSearchBy() != null ? request.getSearchBy() : "name";

        logger.info("Searching users with query: {} by {}", query, searchBy);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query is required");
        }

        List<User> results = users.values().stream()
            .filter(user -> {
                String value = String.valueOf(user.get(searchBy));
                return value.toLowerCase().contains(query.toLowerCase());
            })
            .map(User::fromMap)
            .collect(Collectors.toList());

        return new SearchUserResponse(query, results);
    }

    @Intent("export")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users/export")
    )
    public Map<String, Object> exportUsers(Map<String, Object> payload) {
        String format = (String) payload.getOrDefault("format", "json");

        logger.info("Exporting users in format: {}", format);

        Map<String, Object> response = new HashMap<>();
        response.put("format", format);
        response.put("users", users.values());
        response.put("exportedAt", System.currentTimeMillis());
        response.put("count", users.size());

        return response;
    }

    @Intent("validate")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users/validate"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.validate")
        }
    )
    public ValidateUserResponse validateUser(@RequestBody ValidateUserRequest request) {
        logger.info("Validating user data: {}", request);

        Map<String, String> errors = new HashMap<>();

        String name = request.getName();
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Name is required");
        }

        String email = request.getEmail();
        if (email == null || !email.contains("@")) {
            errors.put("email", "Valid email is required");
        }

        return new ValidateUserResponse(errors.isEmpty(), errors);
    }

    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.get")
        }
    )
    public User getUser(GetUserRequest request) {
        Long id = request.getId();
        logger.info("Getting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.get(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        return User.fromMap(userMap);
    }

    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PUT /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PATCH /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.update")
        }
    )
    public User updateUser(UpdateUserRequest request) {
        Long id = request.getId();
        logger.info("Updating user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.get(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        // Update fields if provided
        String name = request.getName();
        if (name != null) {
            userMap.put("name", name);
        }

        String email = request.getEmail();
        if (email != null) {
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }
            userMap.put("email", email);
        }

        userMap.put("updatedAt", System.currentTimeMillis());

        User user = User.fromMap(userMap);
        logger.info("User updated: {}", user);
        return user;
    }

    @Intent("delete")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "DELETE /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.delete")
        }
    )
    public DeleteUserResponse deleteUser(DeleteUserRequest request) {
        Long id = request.getId();
        logger.info("Deleting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.remove(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        User user = User.fromMap(userMap);
        return new DeleteUserResponse(user);
    }

    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.list")
        }
    )
    public UserListResponse listUsers(Object request) {
        logger.info("Listing all users");

        List<User> userList = users.values().stream()
            .map(User::fromMap)
            .collect(Collectors.toList());

        return new UserListResponse(userList);
    }
}
