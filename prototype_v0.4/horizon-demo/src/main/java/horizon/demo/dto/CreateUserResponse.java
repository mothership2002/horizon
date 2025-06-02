package horizon.demo.dto;

/**
 * DTO for user creation response.
 */
public class CreateUserResponse {
    private String userId;
    private String name;
    private String email;
    private long createdAt;
    private boolean success;

    // Default constructor
    public CreateUserResponse() {}

    public CreateUserResponse(String userId, String name, String email, long createdAt, boolean success) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.success = success;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "CreateUserResponse{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", success=" + success +
                '}';
    }
}
