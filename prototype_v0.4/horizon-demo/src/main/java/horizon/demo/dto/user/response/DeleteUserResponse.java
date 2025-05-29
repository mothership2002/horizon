package horizon.demo.dto.user.response;

import horizon.demo.dto.user.User;

/**
 * DTO for delete user response.
 */
public class DeleteUserResponse {
    private User user;
    private boolean success;

    // Default constructor for Jackson
    public DeleteUserResponse() {
    }

    public DeleteUserResponse(User user) {
        this.user = user;
        this.success = true;
    }

    // Getters and setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}