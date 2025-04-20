package horizon.web.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;

public interface BuildStage<I extends RawInput, O extends RawOutput> {
    AbstractRendezvous<I, O> build();
}
