package horizon.core.flow.foyer;

import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolFoyer<T extends RawInput> implements ProtocolFoyer<T> {

    private final static Logger log = LoggerFactory.getLogger(AbstractProtocolFoyer.class);
    protected final AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous;

    /**
     * Initializes an AbstractProtocolFoyer with the specified protocol rendezvous.
     *
     * @param rendezvous the protocol rendezvous instance that manages protocol interactions for this foyer
     */
    protected AbstractProtocolFoyer(AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous) {
        this.rendezvous = rendezvous;
        log.info("Initializing ProtocolFoyer : {}", getClass().getSimpleName());
    }
}
