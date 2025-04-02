package horizon.core.flow.centinel;

import horizon.core.model.Raw;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinelInterface<T extends Raw> {

    interface OutboundSentinel<T extends RawOutput> extends FlowSentinelInterface<T> {

        void onOutbound(RawOutput rawOutput);
    }

    interface InboundSentinel<T extends RawInput> extends FlowSentinelInterface<T> {

        void onInbound(T rawInput);
    }

}
