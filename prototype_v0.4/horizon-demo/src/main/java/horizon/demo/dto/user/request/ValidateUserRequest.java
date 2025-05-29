package horizon.demo.dto.user.request;

/**
 * DTO for validating user data.
 */
public class ValidateUserRequest {
    private String name;
    private String email;

    // Default constructor for Jackson
    public ValidateUserRequest() {
    }

    public ValidateUserRequest(String name, String email) {
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