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
    public Map<String, Object> createUser(Map<String, Object> payload) {
        logger.info("Creating user with data: {}", payload);
        
        // Extract and validate
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
        
        // Store
        users.put(id, user);
        
        logger.info("User created: {}", user);
        return user;
    }
    
    @Intent("bulkCreate")
    @HttpMapping(methods = "POST", path = "/users/bulk-create")
    public Map<String, Object> bulkCreateUsers(Map<String, Object> payload) {
        logger.info("Bulk creating users");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> userList = (List<Map<String, Object>>) payload.get("users");
        
        if (userList == null || userList.isEmpty()) {
            throw new IllegalArgumentException("Users list is required");
        }
        
        List<Map<String, Object>> createdUsers = new ArrayList<>();
        for (Map<String, Object> userData : userList) {
            try {
                Map<String, Object> created = createUser(userData);
                createdUsers.add(created);
            } catch (Exception e) {
                logger.error("Failed to create user: {}", userData, e);
                // Continue with other users
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("created", createdUsers.size());
        response.put("users", createdUsers);
        
        return response;
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
    public Map<String, Object> searchUsers(Map<String, Object> payload) {
        String query = (String) payload.get("q");
        String searchBy = (String) payload.getOrDefault("searchBy", "name");
        
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
    public Map<String, Object> getUser(Map<String, Object> payload) {
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
    
    @Intent("update")
    @HttpMapping(methods = {"PUT", "PATCH"}, path = "/users/{id}")
    public Map<String, Object> updateUser(Map<String, Object> payload) {
        Long id = extractId(payload);
        logger.info("Updating user with id: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        Map<String, Object> user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        
        // Update fields if provided
        if (payload.containsKey("name")) {
            user.put("name", payload.get("name"));
        }
        if (payload.containsKey("email")) {
            String email = (String) payload.get("email");
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }
            user.put("email", email);
        }
        
        user.put("updatedAt", System.currentTimeMillis());
        
        logger.info("User updated: {}", user);
        return user;
    }
    
    @Intent("delete")
    @HttpMapping(methods = "DELETE", path = "/users/{id}")
    public Map<String, Object> deleteUser(Map<String, Object> payload) {
        Long id = extractId(payload);
        logger.info("Deleting user with id: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        Map<String, Object> user = users.remove(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", true);
        response.put("user", user);
        
        return response;
    }
    
    @Intent("list")
    @HttpMapping(methods = "GET", path = "/users")
    public Map<String, Object> listUsers(Map<String, Object> payload) {
        logger.info("Listing all users");
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users.values());
        response.put("count", users.size());
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
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
