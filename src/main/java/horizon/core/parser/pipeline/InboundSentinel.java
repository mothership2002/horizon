package horizon.core.parser.pipeline;

import horizon.core.input.RawInput;

public interface InboundSentinel<T extends RawInput> extends ProtocolPipeline.SentinelInterface {

    void onInbound(T rawInput);
}
