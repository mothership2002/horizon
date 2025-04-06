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
     * Constructs an AbstractProtocolFoyer with the specified rendezvous.
     *
     * <p>This constructor assigns the provided protocol rendezvous instance to the foyer 
     * and logs an initialization message that includes the simple name of the concrete class.</p>
     *
     * @param rendezvous the protocol rendezvous instance to be associated with this foyer
     */
    protected AbstractProtocolFoyer(AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous) {
        this.rendezvous = rendezvous;
        log.info("Initializing ProtocolFoyer : {}", getClass().getSimpleName());
    }
}
