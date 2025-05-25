package horizon.demo.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO representing a user in the system.
 */
public class User {
    private Long id;
    private String name;
    private String email;
    private Long createdAt;
    private Long updatedAt;

    // Default constructor for Jackson
    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Convert this DTO to a Map for backward compatibility.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        map.put("createdAt", createdAt);
        if (updatedAt != null) {
            map.put("updatedAt", updatedAt);
        }
        return map;
    }

    /**
     * Create a User from a Map for backward compatibility.
     */
    public static User fromMap(Map<String, Object> map) {
        User user = new User();
        user.setId(map.get("id") instanceof Number ? ((Number) map.get("id")).longValue() : null);
        user.setName((String) map.get("name"));
        user.setEmail((String) map.get("email"));
        user.setCreatedAt(map.get("createdAt") instanceof Number ? ((Number) map.get("createdAt")).longValue() : null);
        user.setUpdatedAt(map.get("updatedAt") instanceof Number ? ((Number) map.get("updatedAt")).longValue() : null);
        return user;
    }
}