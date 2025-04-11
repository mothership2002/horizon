package horizon.core.rendezvous.sentinel;

import horizon.core.model.RawInput;


public interface InboundSentinel<I extends RawInput> extends FlowSentinel {

    void inspectInbound(I rawInput);
}
