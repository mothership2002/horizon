package horizon.core.exception;

/**
 * Base exception for all Horizon Framework exceptions.
 */
public class HorizonException extends RuntimeException {
    
    public HorizonException(String message) {
        super(message);
    }
    
    public HorizonException(String message, Throwable cause) {
        super(message, cause);
    }
}
