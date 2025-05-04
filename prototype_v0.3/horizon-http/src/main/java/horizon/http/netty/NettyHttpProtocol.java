package horizon.http.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the NettyProtocol for HTTP.
 * This class extends the generic NettyProtocol to provide HTTP-specific functionality.
 */
public class NettyHttpProtocol extends NettyProtocol {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpProtocol.class);
    private static final String PROTOCOL_NAME = "http";

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
    public void initialize() {
        LOGGER.debug("Initializing Netty HTTP protocol");
        super.initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        LOGGER.debug("Shutting down Netty HTTP protocol");
        super.shutdown();
    }
}
