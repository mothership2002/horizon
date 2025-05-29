package horizon.web.common;

import horizon.core.Foyer;
import horizon.core.Rendezvous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base class for all foyers.
 * This class provides common functionality for foyers without any specific technology dependencies.
 *
 * @param <I> the protocol-specific input type
 */
public abstract class AbstractFoyer<I> implements Foyer<I> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFoyer.class);
    
    protected final int port;
    protected final AtomicBoolean isOpen = new AtomicBoolean(false);
    protected Rendezvous<I, ?> rendezvous;
    
    public AbstractFoyer(int port) {
        this.port = port;
    }
    
    @Override
    public boolean isOpen() {
        return isOpen.get();
    }
    
    @Override
    public void connectToRendezvous(Rendezvous<I, ?> rendezvous) {
        this.rendezvous = rendezvous;
    }
    
    /**
     * Gets the name of the protocol for logging purposes.
     */
    protected abstract String getProtocolName();
}