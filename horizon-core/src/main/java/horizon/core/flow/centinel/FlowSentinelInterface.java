package horizon.core.flow.centinel;

import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.model.Raw;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinelInterface<T extends Raw> {

    interface OutboundSentinel<O extends RawOutput> extends FlowSentinelInterface<O> {

        void onOutbound(O rawOutput) throws OutboundSentinelException;
    }

    interface InboundSentinel<I extends RawInput> extends FlowSentinelInterface<I> {

        void onInbound(I rawInput) throws InboundSentinelException;
    }

}
