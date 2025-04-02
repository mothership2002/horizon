package horizon.parser.pipeline;

import horizon.core.conductor.ConductorManager;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.flow.parser.interpreter.ParsedRequest;
import horizon.core.flow.parser.interpreter.ProtocolInterpreter;
import horizon.core.model.output.RawOutput;
import horizon.core.flow.parser.normalizer.NormalizedInput;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.parser.pipeline.ProtocolPipeline;

import java.util.LinkedList;
import java.util.List;

public class DefaultProtocolPipeline<T extends RawInput, S extends RawOutput> implements ProtocolPipeline<T, S> {

    private final List<FlowSentinelInterface.InboundSentinel<T>> inboundSentinels = new LinkedList<>();
    private final List<FlowSentinelInterface.OutboundSentinel<S>> outboundSentinels = new LinkedList<>();

    private final ProtocolNormalizer<T> normalizer;
    private final ProtocolInterpreter interpreter;
    private final ConductorManager brokerManager;
    private final RawOutputBuilder<S> rawOutputBuilder;

    public DefaultProtocolPipeline(ProtocolNormalizer<T> normalizer, ProtocolInterpreter interpreter, ConductorManager brokerManager, RawOutputBuilder<S> rawOutputBuilder) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.brokerManager = brokerManager;
        this.rawOutputBuilder = rawOutputBuilder;
    }

    public S handle(T rawInput) {
        preprocess(rawInput);
        NormalizedInput normalized = normalizer.normalize(rawInput);
        ParsedRequest parsed = interpreter.interpret(normalized);
        Object result = brokerManager.handle(parsed);
        S output = rawOutputBuilder.build(result);
        postprocess(output);
        return output;
    }

    protected void preprocess(T rawInput) {
        for (FlowSentinelInterface.InboundSentinel<T> s : inboundSentinels) s.onInbound(rawInput);
    }

    protected void postprocess(RawOutput output) {
        for (FlowSentinelInterface.OutboundSentinel<S> s : outboundSentinels) s.onOutbound(output);
    }

    public void addInboundSentinel(FlowSentinelInterface.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    public void addOutboundSentinel(FlowSentinelInterface.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
