package horizon.core.flow.sentinel;

import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOutboundSentinel<O extends RawOutput> implements FlowSentinel.OutboundSentinel<O> {

    private final Logger log = LoggerFactory.getLogger(AbstractOutboundSentinel.class);

    /**
     * Constructs a new AbstractOutboundSentinel instance and logs its creation.
     *
     * <p>This constructor logs an informational message that includes the simple name
     * of the instantiated class.</p>
     */
    public AbstractOutboundSentinel() {
        log.info("Outbound Sentinel created : {}", getClass().getSimpleName());
    }
}
