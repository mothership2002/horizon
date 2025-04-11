package horizon.core.rendezvous.sentinel;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

public interface SurroundSentinel<I extends RawInput, O extends RawOutput> extends InboundSentinel<I>, OutboundSentinel<O> {


}
