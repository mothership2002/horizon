package horizon.core.parser.pipeline;

import horizon.core.output.RawOutput;

public interface OutboundSentinel extends ProtocolPipeline.SentinelInterface {

    void onOutbound(RawOutput rawOutput);
}
