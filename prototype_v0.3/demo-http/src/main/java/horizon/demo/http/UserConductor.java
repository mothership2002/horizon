package horizon.demo.http;

import horizon.core.annotation.ActionType;
import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.command.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sample Conductor that demonstrates how to use the @Conductor and @Intent annotations.
 * This class handles user-related operations like getting user details, creating users, etc.
 */
@Conductor(namespace = "users")
public class UserConductor implements horizon.core.conductor.Conductor<Map<String, Object>> {

    // Simulated user database
    private static final Map<String, Map<String, Object>> userDb = new ConcurrentHashMap<>();

    static {
        // Add some sample users
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "1");
        user1.put("name", "John Doe");
        user1.put("email", "john@example.com");
        userDb.put("1", user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "2");
        user2.put("name", "Jane Smith");
        user2.put("email", "jane@example.com");
        userDb.put("2", user2);
    }

    /**
     * Get all users.
     * 
     * @return a command that returns all users
     */
    @Intent(value = "getAllUsers", metadata = {"action=read", "target=users"})
    public Command<Map<String, Object>> getAllUsers() {
        return new Command<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() {
                Map<String, Object> result = new HashMap<>();
                result.put("users", userDb.values());
                return result;
            }

            @Override
            public String getKey() {
                return "getAllUsers";
            }
        };
    }

    /**
     * Get a user by ID.
     * 
     * @param payload the request payload containing the user ID
     * @return a command that returns the user with the specified ID
     */
    @Intent(value = "getUserById", metadata = {"action=read", "target=user", "param=id"})
    public Command<Map<String, Object>> getUserById(Map<String, Object> payload) {
        return new Command<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() {
                String id = (String) payload.get("id");
                Map<String, Object> user = userDb.get(id);
                if (user == null) {
                    throw new IllegalArgumentException("User not found: " + id);
                }
                return user;
            }

            @Override
            public String getKey() {
                return "getUserById";
            }
        };
    }

    /**
     * Create a new user.
     * 
     * @param payload the request payload containing the user details
     * @return a command that creates a new user
     */
    @Intent(value = "createUser", metadata = {"action=create", "target=user"})
    public Command<Map<String, Object>> createUser(Map<String, Object> payload) {
        return new Command<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() {
                String id = String.valueOf(userDb.size() + 1);
                Map<String, Object> user = new HashMap<>(payload);
                user.put("id", id);
                userDb.put(id, user);
                return user;
            }

            @Override
            public String getKey() {
                return "createUser";
            }
        };
    }

    /**
     * Update an existing user.
     * 
     * @param payload the request payload containing the user details
     * @return a command that updates an existing user
     */
    @Intent(value = "updateUser", metadata = {"action=update", "target=user", "param=id"})
    public Command<Map<String, Object>> updateUser(Map<String, Object> payload) {
        return new Command<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() {
                String id = (String) payload.get("id");
                Map<String, Object> existingUser = userDb.get(id);
                if (existingUser == null) {
                    throw new IllegalArgumentException("User not found: " + id);
                }

                Map<String, Object> updatedUser = new HashMap<>(payload);
                updatedUser.put("id", id);
                userDb.put(id, updatedUser);
                return updatedUser;
            }

            @Override
            public String getKey() {
                return "updateUser";
            }
        };
    }

    /**
     * Delete a user.
     * 
     * @param payload the request payload containing the user ID
     * @return a command that deletes a user
     */
    @Intent(value = "deleteUser", metadata = {"action=delete", "target=user", "param=id"})
    public Command<Map<String, Object>> deleteUser(Map<String, Object> payload) {
        return new Command<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute() {
                String id = (String) payload.get("id");
                Map<String, Object> user = userDb.remove(id);
                if (user == null) {
                    throw new IllegalArgumentException("User not found: " + id);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "User deleted: " + id);
                return result;
            }

            @Override
            public String getKey() {
                return "deleteUser";
            }
        };
    }

    @Override
    public Command<?> resolve(Map<String, Object> payload) throws IllegalArgumentException, NullPointerException {
        // In a real implementation, this method would use reflection to find the appropriate
        // method based on the request path, HTTP method, etc.
        // For this example, we'll just return a simple command
        return new Command<String>() {
            @Override
            public String execute() {
                return "This is a placeholder implementation. In a real application, " +
                       "the resolve method would determine which @Intent method to call " +
                       "based on the request details.";
            }

            @Override
            public String getKey() {
                return "placeholder";
            }
        };
    }
}
