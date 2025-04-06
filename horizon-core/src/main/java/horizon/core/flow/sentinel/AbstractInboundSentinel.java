package horizon.core.flow.sentinel;

import horizon.core.model.input.RawInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInboundSentinel<I extends RawInput> implements FlowSentinel.InboundSentinel<I> {

    private final Logger log = LoggerFactory.getLogger(AbstractInboundSentinel.class);

    public AbstractInboundSentinel() {
        log.info("Inbound Sentinel created : {}", getClass().getSimpleName());
    }
}
