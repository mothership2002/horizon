package horizon.demo.dto.user.request;

/**
 * DTO for creating a new user.
 */
public class CreateUserRequest {
    private String name;
    private String email;

    // Default constructor for Jackson
    public CreateUserRequest() {
    }

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
}