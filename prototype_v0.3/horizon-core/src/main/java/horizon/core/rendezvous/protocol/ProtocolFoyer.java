package horizon.core.rendezvous.protocol;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.Foyer;
import horizon.core.rendezvous.Rendezvous;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A protocol-agnostic implementation of the Foyer interface.
 * This class acts as a bridge between any protocol and the Rendezvous component,
 * allowing the Horizon framework to handle requests from different protocols.
 *
 * @param <I> the type of raw input this foyer can handle
 * @param <O> the type of raw output this foyer produces
 * @param <M> the type of protocol-specific incoming message
 * @param <R> the type of protocol-specific outgoing response
 */
public abstract class ProtocolFoyer<I extends RawInput, O extends RawOutput, M, R> implements Foyer<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolFoyer.class);

    protected final int port;
    protected final Rendezvous<I, O> rendezvous;
    protected final ProtocolAdapter<I, O, M, R> adapter;
    protected final Protocol protocol;
    
    protected boolean initialized = false;

    /**
     * Creates a new ProtocolFoyer with the specified port, rendezvous, adapter, and protocol.
     *
     * @param port the port to listen on, or 0 to use the protocol's default port
     * @param rendezvous the rendezvous to pass requests to
     * @param adapter the adapter to convert between protocol-specific messages and Horizon's RawInput/RawOutput
     * @param protocol the protocol implementation
     * @throws NullPointerException if rendezvous, adapter, or protocol is null
     * @throws IllegalArgumentException if port is invalid
     */
    public ProtocolFoyer(int port, Rendezvous<I, O> rendezvous, ProtocolAdapter<I, O, M, R> adapter, Protocol protocol) {
        this.rendezvous = Objects.requireNonNull(rendezvous, "rendezvous must not be null");
        this.adapter = Objects.requireNonNull(adapter, "adapter must not be null");
        this.protocol = Objects.requireNonNull(protocol, "protocol must not be null");
        
        // Use the protocol's default port if port is 0
        this.port = (port == 0) ? protocol.getDefaultPort() : port;
        
        // Validate the port
        if (this.port <= 0 || this.port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + this.port);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allow(I input) {
        // Default implementation allows all inputs
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        if (initialized) {
            LOGGER.warn(protocol.getName() + " foyer is already initialized");
            return;
        }

        LOGGER.info("Initializing " + protocol.getName() + " foyer on port " + port);
        
        try {
            // Initialize the protocol
            protocol.initialize();
            
            // Initialize the server
            initializeServer();
            
            initialized = true;
            LOGGER.info(protocol.getName() + " foyer initialized and listening on port " + port);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize " + protocol.getName() + " foyer: " + e.getMessage());
            shutdown();
            throw new RuntimeException("Failed to initialize " + protocol.getName() + " foyer", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (!initialized) {
            LOGGER.warn(protocol.getName() + " foyer is not initialized");
            return;
        }

        LOGGER.info("Shutting down " + protocol.getName() + " foyer");
        
        try {
            // Shutdown the server
            shutdownServer();
            
            // Shutdown the protocol
            protocol.shutdown();
            
            initialized = false;
            LOGGER.info(protocol.getName() + " foyer shut down successfully");
        } catch (Exception e) {
            LOGGER.error("Error shutting down " + protocol.getName() + " foyer: " + e.getMessage());
        }
    }

    /**
     * Returns whether this foyer is initialized.
     *
     * @return true if this foyer is initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the port this foyer is listening on.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the protocol this foyer is using.
     *
     * @return the protocol
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * Initializes the server for this protocol.
     * This method should be implemented by subclasses to set up the server for the specific protocol.
     *
     * @throws Exception if an error occurs during initialization
     */
    protected abstract void initializeServer() throws Exception;

    /**
     * Shuts down the server for this protocol.
     * This method should be implemented by subclasses to clean up the server for the specific protocol.
     *
     * @throws Exception if an error occurs during shutdown
     */
    protected abstract void shutdownServer() throws Exception;

    /**
     * Handles an incoming message from the protocol.
     * This method should be called by the protocol-specific server when a message is received.
     *
     * @param message the protocol-specific message
     * @param remoteAddress the remote address of the client
     * @param context any additional context needed for the response
     * @return the protocol-specific response
     */
    protected R handleMessage(M message, String remoteAddress, Object context) {
        try {
            // Convert the message to raw input
            I input = adapter.convertToInput(message, remoteAddress);
            
            // Check if the request should be allowed
            if (!allow(input)) {
                LOGGER.warn("Request from " + remoteAddress + " was denied by the foyer");
                return adapter.createForbiddenResponse(context);
            }
            
            // Process the request through the rendezvous
            var horizonContext = rendezvous.encounter(input);
            
            // If the context has a failure cause, return an error response
            if (horizonContext.getFailureCause() != null) {
                LOGGER.warn("Error processing request: " + horizonContext.getFailureCause().getMessage());
                return adapter.createErrorResponse(horizonContext.getFailureCause(), context);
            }
            
            // Get the output from the context
            O output = rendezvous.fallAway(horizonContext);
            
            // Convert the output to a protocol-specific response
            return adapter.convertToResponse(output, context);
        } catch (Exception e) {
            LOGGER.error("Error handling message: " + e.getMessage());
            return adapter.createErrorResponse(e, context);
        }
    }
}