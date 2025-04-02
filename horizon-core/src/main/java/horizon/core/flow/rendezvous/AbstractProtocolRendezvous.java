package horizon.core.flow.rendezvous;

import horizon.core.conductor.ConductorManager;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractProtocolRendezvous<T extends RawInput, S extends RawOutput> implements ProtocolRendezvous<T, S> {

    protected final List<FlowSentinelInterface.InboundSentinel<T>> inboundSentinels = new LinkedList<>();
    protected final List<FlowSentinelInterface.OutboundSentinel<S>> outboundSentinels = new LinkedList<>();

    protected final AbstractProtocolNormalizer<T> normalizer;
    protected final AbstractProtocolInterpreter interpreter;
    protected final ConductorManager conductorManager;
    protected final RawOutputBuilder<S> rawOutputBuilder;

    public AbstractProtocolRendezvous(AbstractProtocolNormalizer<T> normalizer, AbstractProtocolInterpreter interpreter,
                                      ConductorManager conductorManager, RawOutputBuilder<S> rawOutputBuilder) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.conductorManager = conductorManager;
        this.rawOutputBuilder = rawOutputBuilder;
    }
// TODO 쓰레드풀 구현해야함

    protected S afterConduct(ParsedRequest parsed) {
        Object result = conductorManager.conduct(parsed);
        S output = rawOutputBuilder.build(result);
        postInspect(output);
        return output;
    }

    protected void preInspect(T rawInput) {
        for (FlowSentinelInterface.InboundSentinel<T> s : inboundSentinels) s.onInbound(rawInput);
    }

    protected void postInspect(RawOutput output) {
        for (FlowSentinelInterface.OutboundSentinel<S> s : outboundSentinels) s.onOutbound(output);
    }
}
