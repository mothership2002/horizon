package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.parser.pipeline.ProtocolPipeline;

import java.util.List;

public abstract class AbstractHorizonContext<T extends RawInput, S extends RawOutput> implements HorizonContext<T, S> {

    protected abstract ProtocolPipeline<T, S> initializePipeline();

    protected abstract List<FlowSentinelInterface.InboundSentinel<T>> scanInboundSentinels();

    protected abstract List<FlowSentinelInterface.OutboundSentinel<S>> scanOutboundSentinels();

}
