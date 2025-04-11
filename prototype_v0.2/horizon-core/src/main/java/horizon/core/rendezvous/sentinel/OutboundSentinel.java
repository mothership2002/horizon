package horizon.core.rendezvous.sentinel;

import horizon.core.model.RawOutput;

public interface OutboundSentinel<O extends RawOutput> extends FlowSentinel {
    void inspectOutbound(O rawOutput);
}