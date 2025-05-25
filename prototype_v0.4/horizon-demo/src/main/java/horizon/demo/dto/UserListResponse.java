package horizon.demo.dto;

import java.util.List;

/**
 * DTO for user list response.
 */
public class UserListResponse {
    private List<User> users;
    private int count;
    private long timestamp;

    // Default constructor for Jackson
    public UserListResponse() {
    }

    public UserListResponse(List<User> users) {
        this.users = users;
        this.count = users.size();
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}