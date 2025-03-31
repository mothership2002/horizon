package horizon.core.parser.pipeline;

import horizon.core.broker.BrokerManager;
import horizon.core.input.RawInput;
import horizon.core.interpreter.ParsedRequest;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.output.HorizonRawOutputBuilder;
import horizon.core.output.RawOutput;
import horizon.core.parser.normalizer.NormalizedInput;
import horizon.core.parser.normalizer.ProtocolNormalizer;

import java.util.LinkedList;
import java.util.List;

public class ProtocolPipeline<T extends RawInput> {

    private final List<InboundSentinel<T>> inboundSentinels = new LinkedList<>();
    private final List<OutboundSentinel> outboundSentinels = new LinkedList<>();

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
        for (InboundSentinel<T> s : inboundSentinels) s.onInbound(rawInput);
    }

    protected void postprocess(RawOutput output) {
        for (OutboundSentinel s : outboundSentinels) s.onOutbound(output);
    }

    public void addInboundSentinel(InboundSentinel<T> s) {
        inboundSentinels.add(s);
    }

    public void addOutboundSentinel(OutboundSentinel s) {
        outboundSentinels.add(s);
    }

    interface SentinelInterface {

    }
}
