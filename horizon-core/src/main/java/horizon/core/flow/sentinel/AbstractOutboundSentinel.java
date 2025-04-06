package horizon.core.flow.sentinel;

import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOutboundSentinel<O extends RawOutput> implements FlowSentinel.OutboundSentinel<O> {

    private final Logger log = LoggerFactory.getLogger(AbstractOutboundSentinel.class);

    /**
     * Constructs an AbstractOutboundSentinel instance.
     *
     * <p>This constructor logs an informational message indicating that an outbound sentinel has been created,
     * including the simple name of the specific class instance.</p>
     */
    public AbstractOutboundSentinel() {
        log.info("Outbound Sentinel created : {}", getClass().getSimpleName());
    }
}
