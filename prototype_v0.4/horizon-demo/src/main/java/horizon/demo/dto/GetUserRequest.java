package horizon.demo.dto;

/**
 * DTO for getting a user by ID.
 */
public class GetUserRequest {
    private String userId;

    // Default constructor
    public GetUserRequest() {}

    public GetUserRequest(String userId) {
        this.userId = userId;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "GetUserRequest{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
