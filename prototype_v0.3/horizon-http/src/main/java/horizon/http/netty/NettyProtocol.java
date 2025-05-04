package horizon.http.netty;

import horizon.core.rendezvous.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Protocol interface for Netty.
 * This class represents the generic Netty protocol.
 */
public abstract class NettyProtocol implements Protocol {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyProtocol.class);
    private static final String PROTOCOL_NAME = "netty";
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
        LOGGER.debug("Initializing Netty protocol");
        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Netty protocol");
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