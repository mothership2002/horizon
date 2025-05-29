package horizon.demo.dto.user.request;

/**
 * DTO for deleting a user by ID.
 */
public class DeleteUserRequest {
    private Long id;

    // Default constructor for Jackson
    public DeleteUserRequest() {
    }

    public DeleteUserRequest(Long id) {
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