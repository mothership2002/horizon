package horizon.demo.dto;

import java.util.List;

/**
 * DTO for bulk creating users.
 */
public class BulkCreateUserRequest {
    private List<CreateUserRequest> users;

    // Default constructor for Jackson
    public BulkCreateUserRequest() {
    }

    public BulkCreateUserRequest(List<CreateUserRequest> users) {
        this.users = users;
    }

    // Getters and setters
    public List<CreateUserRequest> getUsers() {
        return users;
    }

    public void setUsers(List<CreateUserRequest> users) {
        this.users = users;
    }
}