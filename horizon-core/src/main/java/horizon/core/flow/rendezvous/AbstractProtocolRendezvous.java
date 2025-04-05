package horizon.core.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractProtocolRendezvous<I extends RawInput, O extends RawOutput> implements ProtocolRendezvous<I, O> {

    protected final List<FlowSentinelInterface.InboundSentinel<I>> inboundSentinels = new LinkedList<>();
    protected final List<FlowSentinelInterface.OutboundSentinel<O>> outboundSentinels = new LinkedList<>();

    protected final AbstractProtocolNormalizer<I> normalizer;
    protected final AbstractProtocolInterpreter interpreter;
    protected final AbstractConductorManager conductorManager;
    protected final RawOutputBuilder<O> rawOutputBuilder;
    protected final AbstractShadowStage shadowStage;

    public AbstractProtocolRendezvous(AbstractProtocolNormalizer<I> normalizer, AbstractProtocolInterpreter interpreter,
                                      AbstractConductorManager conductorManager, RawOutputBuilder<O> rawOutputBuilder, AbstractShadowStage shadowStage) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.conductorManager = conductorManager;
        this.rawOutputBuilder = rawOutputBuilder;
        this.shadowStage = shadowStage;
    }

    protected O afterConduct(ParsedRequest parsed) {
        Object result = conductorManager.conduct(parsed);
        O output = rawOutputBuilder.build(result);
        postInspect(output);
        return output;
    }

    protected void preInspect(I rawInput) {
        try {
            for (FlowSentinelInterface.InboundSentinel<I> s : inboundSentinels) {
                s.onInbound(rawInput);
            }
        } catch (InboundSentinelException e) {

        }
    }

    protected void postInspect(O output) {
        try {
            for (FlowSentinelInterface.OutboundSentinel<O> s : outboundSentinels) {
                s.onOutbound(output);
            }
        } catch (OutboundSentinelException e) {

        }
    }
}
