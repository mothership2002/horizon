package horizon.core.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.interpreter.AbstractInterpreter;
import horizon.core.rendezvous.normalizer.Normalizer;
import horizon.core.rendezvous.sentinel.InboundSentinel;
import horizon.core.rendezvous.sentinel.OutboundSentinel;

import java.util.List;

public abstract class AbstractRendezvous<I extends RawInput, O extends RawOutput> implements Rendezvous<I, O> {

    private final List<InboundSentinel<I>> inboundSentinels;
    private final List<OutboundSentinel<O>> outboundSentinels;
    private final Normalizer normalizer;
    private final AbstractInterpreter interpreter;

    protected AbstractRendezvous(List<InboundSentinel<I>> inboundSentinels, List<OutboundSentinel<O>> outboundSentinels, Normalizer normalizer, AbstractInterpreter interpreter) {
        this.inboundSentinels = inboundSentinels;
        this.outboundSentinels = outboundSentinels;
        this.normalizer = normalizer;
        this.interpreter = interpreter;
    }

}
