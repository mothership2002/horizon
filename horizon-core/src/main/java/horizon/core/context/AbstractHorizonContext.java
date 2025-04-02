package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;

import java.util.List;

public abstract class AbstractHorizonContext<T extends RawInput, S extends RawOutput> implements HorizonContext<T, S> {

    protected abstract AbstractProtocolRendezvous<T, S> initializePipeline();

    protected abstract List<FlowSentinelInterface.InboundSentinel<T>> scanInboundSentinels();

    protected abstract List<FlowSentinelInterface.OutboundSentinel<S>> scanOutboundSentinels();

}
