package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.pipeline.SentinelInterface;

import java.util.List;

public abstract class AbstractHorizonContext<T extends RawInput, S extends RawOutput> implements HorizonContext<T, S> {

    abstract ProtocolPipeline<T, S> initializePipeline();

    protected abstract List<SentinelInterface.InboundSentinel<T>> scanInboundSentinels();

    protected abstract List<SentinelInterface.OutboundSentinel<S>> scanOutboundSentinels();

}
