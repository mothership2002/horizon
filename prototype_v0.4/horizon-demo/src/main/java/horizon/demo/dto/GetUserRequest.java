package horizon.demo.dto;

/**
 * DTO for getting a user by ID.
 */
public class GetUserRequest {
    private Long id;

    // Default constructor for Jackson
    public GetUserRequest() {
    }

    public GetUserRequest(Long id) {
        this.id = id;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}