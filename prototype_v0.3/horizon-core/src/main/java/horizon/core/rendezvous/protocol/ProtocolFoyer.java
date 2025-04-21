package horizon.core.rendezvous.protocol;

import horizon.core.context.HorizonSystemContext;
import horizon.core.engine.HorizonFlowEngine;
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
    protected final HorizonSystemContext systemContext;

    protected HorizonFlowEngine flowEngine;
    protected boolean initialized = false;

    /**
     * Creates a new ProtocolFoyer with the specified port, rendezvous, adapter, protocol, and system context.
     *
     * @param port the port to listen on, or 0 to use the protocol's default port
     * @param rendezvous the rendezvous to pass requests to
     * @param adapter the adapter to convert between protocol-specific messages and Horizon's RawInput/RawOutput
     * @param protocol the protocol implementation
     * @param systemContext the system context, or null if not available
     * @throws NullPointerException if rendezvous, adapter, or protocol is null
     * @throws IllegalArgumentException if port is invalid
     */
    public ProtocolFoyer(int port, Rendezvous<I, O> rendezvous, ProtocolAdapter<I, O, M, R> adapter, Protocol protocol, HorizonSystemContext systemContext) {
        this.rendezvous = Objects.requireNonNull(rendezvous, "rendezvous must not be null");
        this.adapter = Objects.requireNonNull(adapter, "adapter must not be null");
        this.protocol = Objects.requireNonNull(protocol, "protocol must not be null");
        this.systemContext = systemContext;

        // Use the protocol's default port if port is 0
        this.port = (port == 0) ? protocol.getDefaultPort() : port;

        // Validate the port
        if (this.port <= 0 || this.port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + this.port);
        }
    }

    /**
     * Creates a new ProtocolFoyer with the specified port, rendezvous, adapter, and protocol.
     * This constructor is provided for backward compatibility.
     *
     * @param port the port to listen on, or 0 to use the protocol's default port
     * @param rendezvous the rendezvous to pass requests to
     * @param adapter the adapter to convert between protocol-specific messages and Horizon's RawInput/RawOutput
     * @param protocol the protocol implementation
     * @throws NullPointerException if rendezvous, adapter, or protocol is null
     * @throws IllegalArgumentException if port is invalid
     */
    public ProtocolFoyer(int port, Rendezvous<I, O> rendezvous, ProtocolAdapter<I, O, M, R> adapter, Protocol protocol) {
        this(port, rendezvous, adapter, protocol, null);
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
            LOGGER.warn("{} foyer is already initialized", protocol.getName());
            return;
        }

        LOGGER.info("Initializing {} foyer on port {}", protocol.getName(), port);

        try {
            // Initialize the protocol
            protocol.initialize();

            // Initialize the server
            initializeServer();

            initialized = true;
            LOGGER.info("{} foyer initialized and listening on port {}", protocol.getName(), port);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize {} foyer: {}", protocol.getName(), e.getMessage());
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
     * Sets the Flow Engine to be used by this foyer.
     * If set, the Flow Engine will be used to process requests instead of directly using the Rendezvous.
     *
     * @param flowEngine the Flow Engine to use
     */
    public void setFlowEngine(HorizonFlowEngine flowEngine) {
        this.flowEngine = flowEngine;
        LOGGER.info("Flow Engine set for {} foyer", protocol.getName());
    }

    /**
     * Returns the Flow Engine used by this foyer.
     *
     * @return the Flow Engine, or null if not set
     */
    public HorizonFlowEngine getFlowEngine() {
        return flowEngine;
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
                LOGGER.warn("Request from {} was denied by the foyer", remoteAddress);
                return adapter.createForbiddenResponse(context);
            }

            O output;

            // Try to get the Flow Engine from the system context first, then fall back to the directly set Flow Engine
            HorizonFlowEngine engine = null;
            if (systemContext != null) {
                try {
                    engine = systemContext.getFlowEngine();
                    LOGGER.debug("Using Flow Engine from system context");
                } catch (Exception e) {
                    LOGGER.warn("Error getting Flow Engine from system context: {}", e.getMessage());
                }
            }

            // If no Flow Engine from system context, use the directly set Flow Engine
            if (engine == null) {
                engine = flowEngine;
                if (engine != null) {
                    LOGGER.debug("Using directly set Flow Engine");
                }
            }

            // If Flow Engine is available, use it to process the request
            if (engine != null) {
                LOGGER.debug("Processing request using Flow Engine");
                output = (O) engine.run(input);
            } else {
                // Otherwise, use the rendezvous directly
                LOGGER.debug("Processing request using Rendezvous directly");
                var horizonContext = rendezvous.encounter(input);

                // If the context has a failure cause, return an error response
                if (horizonContext.getFailureCause() != null) {
                    LOGGER.warn("Error processing request: {}", horizonContext.getFailureCause().getMessage());
                    return adapter.createErrorResponse(horizonContext.getFailureCause(), context);
                }

                // Get the output from the context
                output = rendezvous.fallAway(horizonContext);
            }

            // Convert the output to a protocol-specific response
            return adapter.convertToResponse(output, context);
        } catch (Exception e) {
            LOGGER.error("Error handling message: {}", e.getMessage());
            LOGGER.error("",e);
            return adapter.createErrorResponse(e, context);
        }
    }
}
