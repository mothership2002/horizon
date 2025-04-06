package horizon.core.flow.rendezvous;

import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolRendezvous<T extends RawInput, S extends RawOutput> {

    CompletableFuture<S> encounter(T rawInput);

    void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel);

    void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel);
}
