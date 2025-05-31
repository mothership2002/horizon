package horizon.core.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails.
 * Contains field-level validation errors.
 */
public class ValidationException extends HorizonException {
    private final Map<String, String> errors;
    
    public ValidationException(Map<String, String> errors) {
        super("Validation failed: " + errors);
        this.errors = errors;
    }
    
    public ValidationException(String field, String message) {
        this(Map.of(field, message));
    }
    
    public Map<String, String> getErrors() {
        return errors;
    }
}
