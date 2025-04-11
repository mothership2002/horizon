package horizon.core.flow.sentinel;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSentinel<I extends RawInput, O extends RawOutput> implements FlowSentinel.InboundSentinel<I>, FlowSentinel.OutboundSentinel<O> {

    private final Logger log = LoggerFactory.getLogger(AbstractSentinel.class);

    /**
     * Constructs a new AbstractSentinel instance and logs its creation.
     *
     * <p>This constructor logs a message displaying the simple name of the class to indicate that a new sentinel instance has been created.</p>
     */
    public AbstractSentinel() {
        log.info("Total Sentinel created : {}", getClass().getSimpleName());
    }
}
