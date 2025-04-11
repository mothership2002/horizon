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
     * Constructs an AbstractProtocolFoyer with the specified protocol rendezvous.
     *
     * <p>This constructor assigns the provided rendezvous instance to the foyer and logs an informational message
     * that includes the simple name of the class.
     *
     * @param rendezvous the protocol rendezvous instance used to facilitate communication between the foyer and protocol components
     */
    protected AbstractProtocolFoyer(AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous) {
        this.rendezvous = rendezvous;
        log.info("Initializing ProtocolFoyer : {}", getClass().getSimpleName());
    }
}
