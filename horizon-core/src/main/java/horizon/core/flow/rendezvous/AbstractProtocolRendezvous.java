package horizon.core.flow.rendezvous;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.constant.Scheme;
import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;
import horizon.core.util.SentinelScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class AbstractProtocolRendezvous<I extends RawInput, O extends RawOutput> implements ProtocolRendezvous<I, O> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolRendezvous.class);

    protected final List<FlowSentinel.InboundSentinel<I>> inboundSentinels = new LinkedList<>();
    protected final List<FlowSentinel.OutboundSentinel<O>> outboundSentinels = new LinkedList<>();

    protected final AbstractProtocolNormalizer<I> normalizer;
    protected final AbstractProtocolInterpreter interpreter;
    protected final AbstractConductorManager conductorManager;
    protected final RawOutputBuilder<O> rawOutputBuilder;
    protected final AbstractShadowStage shadowStage;

    public AbstractProtocolRendezvous(AbstractProtocolNormalizer<I> normalizer, AbstractProtocolInterpreter interpreter,
                                      AbstractConductorManager conductorManager, RawOutputBuilder<O> rawOutputBuilder,
                                      AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner sentinelScanner) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.conductorManager = conductorManager;
        this.rawOutputBuilder = rawOutputBuilder;
        this.shadowStage = shadowStage;
        sentinelScanner.getInboundSentinels(scheme.name()).forEach(s -> inboundSentinels.add((FlowSentinel.InboundSentinel<I>) s));
        sentinelScanner.getOutboundSentinels(scheme.name()).forEach(s -> outboundSentinels.add((FlowSentinel.OutboundSentinel<O>) s));
        log.info("Creating protocol rendezvous : {}", getClass().getSimpleName());
    }

    protected O afterConduct(ParsedRequest parsed) {
        Object result = conductorManager.conduct(parsed);
        O output = rawOutputBuilder.build(result);
        postInspect(output);
        return output;
    }

    protected void preInspect(I rawInput) {
        try {
            for (FlowSentinel.InboundSentinel<I> s : inboundSentinels) {
                s.inspectInbound(rawInput);
            }
        } catch (InboundSentinelException e) {

        }
    }

    protected void postInspect(O output) {
        try {
            for (FlowSentinel.OutboundSentinel<O> s : outboundSentinels) {
                s.inspectOutbound(output);
            }
        } catch (OutboundSentinelException e) {

        }
    }
}
