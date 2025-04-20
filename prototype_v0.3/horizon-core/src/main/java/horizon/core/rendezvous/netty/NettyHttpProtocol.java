package horizon.core.rendezvous.netty;

import horizon.core.rendezvous.protocol.Protocol;

/**
 * Implementation of the Protocol interface for Netty HTTP.
 * This class represents the HTTP protocol implemented using Netty.
 */
public class NettyHttpProtocol implements Protocol {
    private static final String PROTOCOL_NAME = "http";
    private static final int DEFAULT_PORT = 8080;
    
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