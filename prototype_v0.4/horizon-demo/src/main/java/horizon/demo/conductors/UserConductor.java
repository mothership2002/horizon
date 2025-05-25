package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.HttpMapping;
import horizon.core.annotation.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * User management conductor using annotation-based declaration.
 * This single class handles user-related intents for ALL protocols!
 */
@Conductor(namespace = "user")
public class UserConductor {
    private static final Logger logger = LoggerFactory.getLogger(UserConductor.class);

    // Simple in-memory storage for demo
    private final Map<Long, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000);

    @Intent("create")
    @HttpMapping(methods = "POST", path = "/users")
    public horizon.demo.dto.User createUser(horizon.demo.dto.CreateUserRequest request) {
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
        horizon.demo.dto.User user = new horizon.demo.dto.User(id, name, email);

        // Store
        users.put(id, user.toMap());

        logger.info("User created: {}", user);
        return user;
    }

    // For backward compatibility with Map-based calls
    private horizon.demo.dto.User createUserFromMap(Map<String, Object> payload) {
        horizon.demo.dto.CreateUserRequest request = new horizon.demo.dto.CreateUserRequest();
        request.setName((String) payload.get("name"));
        request.setEmail((String) payload.get("email"));
        return createUser(request);
    }

    @Intent("bulkCreate")
    @HttpMapping(methods = "POST", path = "/users/bulk-create")
    public horizon.demo.dto.BulkCreateUserResponse bulkCreateUsers(horizon.demo.dto.BulkCreateUserRequest request) {
        logger.info("Bulk creating users");

        List<horizon.demo.dto.CreateUserRequest> userRequests = request.getUsers();

        if (userRequests == null || userRequests.isEmpty()) {
            throw new IllegalArgumentException("Users list is required");
        }

        List<horizon.demo.dto.User> createdUsers = new ArrayList<>();
        for (horizon.demo.dto.CreateUserRequest userRequest : userRequests) {
            try {
                horizon.demo.dto.User created = createUser(userRequest);
                createdUsers.add(created);
            } catch (Exception e) {
                logger.error("Failed to create user: {}", userRequest, e);
                // Continue with other users
            }
        }

        return new horizon.demo.dto.BulkCreateUserResponse(createdUsers);
    }

    @Intent("import")
    @HttpMapping(methods = "POST", path = "/users/import")
    public Map<String, Object> importUsers(Map<String, Object> payload) {
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
    @HttpMapping(methods = "GET", path = "/users/search")
    public horizon.demo.dto.SearchUserResponse searchUsers(horizon.demo.dto.SearchUserRequest request) {
        String query = request.getQ();
        String searchBy = request.getSearchBy() != null ? request.getSearchBy() : "name";

        logger.info("Searching users with query: {} by {}", query, searchBy);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query is required");
        }

        List<horizon.demo.dto.User> results = users.values().stream()
            .filter(user -> {
                String value = String.valueOf(user.get(searchBy));
                return value.toLowerCase().contains(query.toLowerCase());
            })
            .map(horizon.demo.dto.User::fromMap)
            .collect(Collectors.toList());

        return new horizon.demo.dto.SearchUserResponse(query, results);
    }

    @Intent("export")
    @HttpMapping(methods = "GET", path = "/users/export")
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
    @HttpMapping(methods = "POST", path = "/users/validate")
    public Map<String, Object> validateUser(Map<String, Object> payload) {
        logger.info("Validating user data: {}", payload);

        Map<String, Object> errors = new HashMap<>();

        String name = (String) payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Name is required");
        }

        String email = (String) payload.get("email");
        if (email == null || !email.contains("@")) {
            errors.put("email", "Valid email is required");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", errors.isEmpty());
        response.put("errors", errors);

        return response;
    }

    @Intent("get")
    @HttpMapping(methods = "GET", path = "/users/{id}")
    public horizon.demo.dto.User getUser(horizon.demo.dto.GetUserRequest request) {
        Long id = request.getId();
        logger.info("Getting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.get(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        return horizon.demo.dto.User.fromMap(userMap);
    }

    @Intent("update")
    @HttpMapping(methods = {"PUT", "PATCH"}, path = "/users/{id}")
    public horizon.demo.dto.User updateUser(horizon.demo.dto.UpdateUserRequest request) {
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

        horizon.demo.dto.User user = horizon.demo.dto.User.fromMap(userMap);
        logger.info("User updated: {}", user);
        return user;
    }

    @Intent("delete")
    @HttpMapping(methods = "DELETE", path = "/users/{id}")
    public horizon.demo.dto.DeleteUserResponse deleteUser(horizon.demo.dto.DeleteUserRequest request) {
        Long id = request.getId();
        logger.info("Deleting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.remove(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        horizon.demo.dto.User user = horizon.demo.dto.User.fromMap(userMap);
        return new horizon.demo.dto.DeleteUserResponse(user);
    }

    @Intent("list")
    @HttpMapping(methods = "GET", path = "/users")
    public horizon.demo.dto.UserListResponse listUsers(Object request) {
        logger.info("Listing all users");

        List<horizon.demo.dto.User> userList = users.values().stream()
            .map(horizon.demo.dto.User::fromMap)
            .collect(Collectors.toList());

        return new horizon.demo.dto.UserListResponse(userList);
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
}
