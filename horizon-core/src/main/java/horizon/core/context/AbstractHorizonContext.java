package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.flow.centinel.SentinelInterface;
import horizon.core.flow.parser.pipeline.ProtocolPipeline;

import java.util.List;

public abstract class AbstractHorizonContext<T extends RawInput, S extends RawOutput> implements HorizonContext<T, S> {

    protected abstract ProtocolPipeline<T, S> initializePipeline();

    protected abstract List<SentinelInterface.InboundSentinel<T>> scanInboundSentinels();

    protected abstract List<SentinelInterface.OutboundSentinel<S>> scanOutboundSentinels();

}
