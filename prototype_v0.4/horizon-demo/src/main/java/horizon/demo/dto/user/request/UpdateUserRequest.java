package horizon.demo.dto.user.request;

/**
 * DTO for updating an existing user.
 */
public class UpdateUserRequest {
    private Long id;
    private String name;
    private String email;

    // Default constructor for Jackson
    public UpdateUserRequest() {
    }

    public UpdateUserRequest(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}