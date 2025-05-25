package horizon.demo.dto;

/**
 * DTO for delete user response.
 */
public class DeleteUserResponse {
    private boolean deleted;
    private User user;

    // Default constructor for Jackson
    public DeleteUserResponse() {
    }

    public DeleteUserResponse(User user) {
        this.deleted = true;
        this.user = user;
    }

    // Getters and setters
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}