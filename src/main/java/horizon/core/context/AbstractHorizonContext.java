package horizon.core.context;

import horizon.core.input.RawInput;
import horizon.core.parser.pipeline.InboundSentinel;
import horizon.core.parser.pipeline.OutboundSentinel;
import horizon.core.parser.pipeline.ProtocolPipeline;

import java.util.List;

public abstract class AbstractHorizonContext<T extends RawInput> implements HorizonContext<T> {

    abstract ProtocolPipeline<T> initializePipeline();

    protected List<InboundSentinel<T>> scanInboundSentinels() {
        return null;
    }

    protected List<OutboundSentinel> scanOutboundSentinels() {
        return null;
    }
}
