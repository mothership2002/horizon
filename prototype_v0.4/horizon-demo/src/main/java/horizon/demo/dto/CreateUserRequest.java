package horizon.demo.dto;

/**
 * DTO for creating a new user.
 * This will be used for all protocols (HTTP, WebSocket, gRPC).
 */
public class CreateUserRequest {
    private String name;
    private String email;

    // Default constructor
    public CreateUserRequest() {}

    public CreateUserRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and setters
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

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
