package horizon.core.exception;

/**
 * Exception thrown when protocol-specific errors occur.
 */
public class ProtocolException extends HorizonException {
    private final String protocol;
    private final String intent;
    
    public ProtocolException(String protocol, String intent, String message) {
        super(String.format("[%s] %s - %s", protocol, intent, message));
        this.protocol = protocol;
        this.intent = intent;
    }
    
    public ProtocolException(String protocol, String intent, String message, Throwable cause) {
        super(String.format("[%s] %s - %s", protocol, intent, message), cause);
        this.protocol = protocol;
        this.intent = intent;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public String getIntent() {
        return intent;
    }
}
