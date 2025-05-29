package horizon.demo.dto.user.response;

import java.util.Map;

/**
 * DTO for validate user response.
 */
public class ValidateUserResponse {
    private boolean valid;
    private Map<String, String> errors;

    // Default constructor for Jackson
    public ValidateUserResponse() {
    }

    public ValidateUserResponse(boolean valid, Map<String, String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    // Getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}