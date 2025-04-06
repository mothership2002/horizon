package horizon.core.flow.sentinel;

import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOutboundSentinel<O extends RawOutput> implements FlowSentinel.OutboundSentinel<O> {

    private final Logger log = LoggerFactory.getLogger(AbstractOutboundSentinel.class);

    /**
     * Constructs an instance of AbstractOutboundSentinel and logs its creation.
     *
     * <p>This constructor logs an informational message, including the simple class name of the concrete
     * outbound sentinel implementation, to indicate that an outbound sentinel has been instantiated.</p>
     */
    public AbstractOutboundSentinel() {
        log.info("Outbound Sentinel created : {}", getClass().getSimpleName());
    }
}
