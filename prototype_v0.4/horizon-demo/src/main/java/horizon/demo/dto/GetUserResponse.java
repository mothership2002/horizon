package horizon.demo.dto;

/**
 * DTO for user get response.
 */
public class GetUserResponse {
    private boolean found;
    private String userId;
    private String name;
    private String email;
    private long createdAt;

    // Default constructor
    public GetUserResponse() {}

    public GetUserResponse(boolean found, String userId, String name, String email, long createdAt) {
        this.found = found;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    // Static factory methods
    public static GetUserResponse notFound() {
        return new GetUserResponse(false, null, null, null, 0);
    }

    public static GetUserResponse found(String userId, String name, String email, long createdAt) {
        return new GetUserResponse(true, userId, name, email, createdAt);
    }

    // Getters and setters
    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

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

    @Override
    public String toString() {
        return "GetUserResponse{" +
                "found=" + found +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
