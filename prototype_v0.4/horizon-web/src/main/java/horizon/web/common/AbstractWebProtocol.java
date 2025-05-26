package horizon.web.common;

import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for web protocols (HTTP and WebSocket).
 * This class provides common functionality for web protocols.
 *
 * @param <I> the protocol-specific input type
 * @param <O> the protocol-specific output type
 */
public abstract class AbstractWebProtocol<I, O> implements Protocol {
    private static final Logger logger = LoggerFactory.getLogger(AbstractWebProtocol.class);
    
    /**
     * Gets the name of the protocol.
     * This is used for protocol registration and access control.
     */
    @Override
    public abstract String getName();
    
    /**
     * Gets a display name for the protocol.
     * This is used for logging and user interfaces.
     */
    @Override
    public String getDisplayName() {
        return getName();
    }
    
    /**
     * Creates a protocol adapter for this protocol.
     * This method is called by the ProtocolAggregator when registering the protocol.
     *
     * @return a new protocol adapter
     */
    public abstract ProtocolAdapter<I, O> createAdapter();
    
    /**
     * Gets the default port for this protocol.
     * This can be overridden by configuration.
     *
     * @return the default port
     */
    public abstract int getDefaultPort();
    
    /**
     * Gets the protocol description.
     * This is used for documentation and logging.
     *
     * @return a description of the protocol
     */
    public String getDescription() {
        return getDisplayName() + " Protocol";
    }
    
    /**
     * Checks if this protocol supports secure connections.
     *
     * @return true if secure connections are supported
     */
    public boolean supportsSecureConnections() {
        return true;
    }
    
    /**
     * Gets the protocol version.
     * This is used for compatibility checking.
     *
     * @return the protocol version
     */
    public String getVersion() {
        return "1.0";
    }
}