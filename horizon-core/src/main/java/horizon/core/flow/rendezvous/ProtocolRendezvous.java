package horizon.core.flow.rendezvous;

import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolRendezvous<T extends RawInput, S extends RawOutput> {

    CompletableFuture<S> encounter(T rawInput);

    void addInboundSentinel(FlowSentinelInterface.InboundSentinel<T> sentinel);

    void addOutboundSentinel(FlowSentinelInterface.OutboundSentinel<S> sentinel);
}
