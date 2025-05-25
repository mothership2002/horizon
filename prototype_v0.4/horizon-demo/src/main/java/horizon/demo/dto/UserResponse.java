package horizon.demo.dto;

/**
 * DTO for user response.
 */
public class UserResponse {
    private User user;

    // Default constructor for Jackson
    public UserResponse() {
    }

    public UserResponse(User user) {
        this.user = user;
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}