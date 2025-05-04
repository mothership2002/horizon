package horizon.core.rendezvous.protocol;

/**
 * Represents a communication protocol in the Horizon framework.
 * This interface defines the basic properties and lifecycle methods for a protocol.
 */
public interface Protocol {

    /**
     * Returns the name of this protocol.
     * The name should be unique and descriptive, e.g., "http", "websocket", "tcp".
     *
     * @return the name of this protocol
     */
    String getName();

    /**
     * Returns the default port for this protocol.
     * This is used as a fallback if no port is specified when creating a protocol adapter.
     *
     * @return the default port for this protocol
     */
    int getDefaultPort();

    /**
     * Initializes this protocol.
     * This method should be called before using the protocol.
     */
    default void initialize() {
        // Default implementation does nothing
    }

    /**
     * Shuts down this protocol.
     * This method should be called when the protocol is no longer needed.
     */
    default void shutdown() {
        // Default implementation does nothing
    }

    /**
     * Returns whether this protocol is initialized.
     *
     * @return true if this protocol is initialized, false otherwise
     */
    boolean isInitialized();
}