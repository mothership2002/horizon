package horizon.core.rendezvous.websocket;

import horizon.core.rendezvous.protocol.Protocol;

/**
 * Implementation of the Protocol interface for WebSocket.
 * This class represents the WebSocket protocol.
 */
public class WebSocketProtocol implements Protocol {
    private static final String PROTOCOL_NAME = "websocket";
    private static final int DEFAULT_PORT = 8081;
    
    private boolean initialized = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        initialized = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized() {
        return initialized;
    }
}