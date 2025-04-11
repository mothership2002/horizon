package horizon.core.flow.sentinel;

import horizon.core.model.input.RawInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInboundSentinel<I extends RawInput> implements FlowSentinel.InboundSentinel<I> {

    private final Logger log = LoggerFactory.getLogger(AbstractInboundSentinel.class);

    /**
     * Instantiates a new AbstractInboundSentinel and logs its creation.
     *
     * <p>This constructor logs an informational message that includes the simple class name
     * of the concrete subclass, thereby assisting in tracking the instantiation of inbound sentinel instances.
     */
    public AbstractInboundSentinel() {
        log.info("Inbound Sentinel created : {}", getClass().getSimpleName());
    }
}
