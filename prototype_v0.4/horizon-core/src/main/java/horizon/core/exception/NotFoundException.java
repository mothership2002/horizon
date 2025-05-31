package horizon.core.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends HorizonException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String resourceType, Object id) {
        super(String.format("%s not found: %s", resourceType, id));
    }
}
