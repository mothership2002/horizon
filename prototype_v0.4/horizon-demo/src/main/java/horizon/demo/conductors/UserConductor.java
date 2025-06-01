package horizon.demo.conductors;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * User management conductor using protocol-neutral @Param annotation.
 * This replaces the deprecated HTTP-specific annotations.
 */
@Conductor(namespace = "user")
public class UserConductor {
    private static final Logger logger = LoggerFactory.getLogger(UserConductor.class);

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
    public Map<String, Object> createUser(
            @Param("name") String name,
            @Param("email") String email
    ) {
        logger.info("Creating user: name={}, email={}", name, email);

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

    @Intent("bulkCreate")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users/bulk-create")
    )
    public Map<String, Object> bulkCreateUsers(@Param("users") List<Map<String, Object>> userRequests) {
        logger.info("Bulk creating {} users", userRequests.size());

        if (userRequests == null || userRequests.isEmpty()) {
            throw new IllegalArgumentException("Users list is required");
        }

        List<Map<String, Object>> createdUsers = new ArrayList<>();
        for (Map<String, Object> userRequest : userRequests) {
            try {
                String name = (String) userRequest.get("name");
                String email = (String) userRequest.get("email");
                Map<String, Object> created = createUser(name, email);
                createdUsers.add(created);
            } catch (Exception e) {
                logger.error("Failed to create user: {}", userRequest, e);
                // Continue with other users
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("created", createdUsers.size());
        response.put("users", createdUsers);
        return response;
    }

    @Intent("import")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "POST /users/import")
    )
    public Map<String, Object> importUsers(
            @Param("source") String source,
            @Param("format") String format
    ) {
        logger.info("Importing users from source: {}, format: {}", source, format);

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
    public Map<String, Object> searchUsers(
            @Param("q") String query,
            @Param(value = "searchBy", defaultValue = "name") String searchBy
    ) {
        logger.info("Searching users with query: {} by {}", query, searchBy);

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query is required");
        }

        List<Map<String, Object>> results = users.values().stream()
            .filter(user -> {
                String value = String.valueOf(user.get(searchBy));
                return value.toLowerCase().contains(query.toLowerCase());
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("results", results);
        response.put("count", results.size());
        return response;
    }

    @Intent("export")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users/export")
    )
    public Map<String, Object> exportUsers(
            @Param(value = "format", defaultValue = "json") String format
    ) {
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
    public Map<String, Object> validateUser(
            @Param(value = "name", required = false) String name,
            @Param(value = "email", required = false) String email
    ) {
        logger.info("Validating user data: name={}, email={}", name, email);

        Map<String, String> errors = new HashMap<>();

        if (name == null || name.trim().isEmpty()) {
            errors.put("name", "Name is required");
        }

        if (email == null || !email.contains("@")) {
            errors.put("email", "Valid email is required");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", errors.isEmpty());
        response.put("errors", errors);

        return response;
    }

    @Intent("get")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.get")
        }
    )
    public Map<String, Object> getUser(@Param("id") Long id) {
        logger.info("Getting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.get(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        return userMap;
    }

    @Intent("update")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PUT /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "PATCH /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.update")
        }
    )
    public Map<String, Object> updateUser(
            @Param("id") Long id,
            @Param(value = "name", required = false) String name,
            @Param(value = "email", required = false) String email
    ) {
        logger.info("Updating user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.get(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        // Update fields if provided
        if (name != null) {
            userMap.put("name", name);
        }

        if (email != null) {
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }
            userMap.put("email", email);
        }

        userMap.put("updatedAt", System.currentTimeMillis());

        logger.info("User updated: {}", userMap);
        return userMap;
    }

    @Intent("delete")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "DELETE /users/{id}"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.delete")
        }
    )
    public Map<String, Object> deleteUser(@Param("id") Long id) {
        logger.info("Deleting user with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Map<String, Object> userMap = users.remove(id);
        if (userMap == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("deleted", true);
        response.put("user", userMap);
        return response;
    }

    @Intent("list")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = ProtocolNames.HTTP, value = "GET /users"),
            @ProtocolSchema(protocol = ProtocolNames.WEBSOCKET, value = "user.list")
        }
    )
    public Map<String, Object> listUsers(
            @Param(value = "page", defaultValue = "1") Integer page,
            @Param(value = "size", defaultValue = "10") Integer size
    ) {
        logger.info("Listing all users - page: {}, size: {}", page, size);

        List<Map<String, Object>> userList = new ArrayList<>(users.values());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", userList);
        response.put("page", page);
        response.put("size", size);
        response.put("total", users.size());

        return response;
    }
}
