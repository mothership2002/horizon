package horizon.core.parser.pipeline;

import horizon.core.broker.BrokerManager;
import horizon.core.model.input.RawInput;
import horizon.core.interpreter.ParsedRequest;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.model.output.HorizonRawOutputBuilder;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.normalizer.NormalizedInput;
import horizon.core.parser.normalizer.ProtocolNormalizer;

import java.util.LinkedList;
import java.util.List;

public class ProtocolPipeline<T extends RawInput, S extends RawOutput> {

    private final List<SentinelInterface.InboundSentinel<T>> inboundSentinels = new LinkedList<>();
    private final List<SentinelInterface.OutboundSentinel<S>> outboundSentinels = new LinkedList<>();

    private final ProtocolNormalizer<T> normalizer;
    private final ProtocolInterpreter interpreter;
    private final BrokerManager brokerManager;

    public ProtocolPipeline(ProtocolNormalizer<T> normalizer, ProtocolInterpreter interpreter, BrokerManager brokerManager) {
        this.normalizer = normalizer;
        this.interpreter = interpreter;
        this.brokerManager = brokerManager;
    }

    public RawOutput handle(T rawInput) {
        preprocess(rawInput);
        NormalizedInput normalized = normalizer.normalize(rawInput);
        ParsedRequest parsed = interpreter.interpret(normalized);
        Object result = brokerManager.handle(parsed);
        RawOutput output = HorizonRawOutputBuilder.build(result);
        postprocess(output);
        return output;
    }

    protected void preprocess(T rawInput) {
        for (SentinelInterface.InboundSentinel<T> s : inboundSentinels) s.onInbound(rawInput);
    }

    protected void postprocess(RawOutput output) {
        for (SentinelInterface.OutboundSentinel<S> s : outboundSentinels) s.onOutbound(output);
    }

    public void addInboundSentinel(SentinelInterface.InboundSentinel<T> sentinel) {
        inboundSentinels.add(sentinel);
    }

    public void addOutboundSentinel(SentinelInterface.OutboundSentinel<S> sentinel) {
        outboundSentinels.add(sentinel);
    }


}
