package horizon.core.context;


import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;

public interface EncounterContext<I extends RawInput, O extends RawOutput> {
    AbstractRendezvous<I, O> rendezvous();
}

