package horizon.core.flow.sentinel;

import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinel {

    interface OutboundSentinel<O extends RawOutput> extends FlowSentinel {
        void inspectOutbound(O rawOutput) throws OutboundSentinelException;
    }

    interface InboundSentinel<I extends RawInput> extends FlowSentinel {
        void inspectInbound(I rawInput) throws InboundSentinelException;
    }
}
