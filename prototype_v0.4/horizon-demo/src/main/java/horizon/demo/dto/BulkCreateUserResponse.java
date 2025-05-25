package horizon.demo.dto;

import java.util.List;

/**
 * DTO for bulk create user response.
 */
public class BulkCreateUserResponse {
    private int created;
    private List<User> users;

    // Default constructor for Jackson
    public BulkCreateUserResponse() {
    }

    public BulkCreateUserResponse(List<User> users) {
        this.users = users;
        this.created = users.size();
    }

    // Getters and setters
    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}