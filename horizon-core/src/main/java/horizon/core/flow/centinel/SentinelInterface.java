package horizon.core.flow.centinel;

import horizon.core.model.Raw;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface SentinelInterface<T extends Raw> {

    public interface OutboundSentinel<T extends RawOutput> extends SentinelInterface<T> {

        void onOutbound(RawOutput rawOutput);
    }

    public interface InboundSentinel<T extends RawInput> extends SentinelInterface<T> {

        void onInbound(T rawInput);
    }

}
