package horizon.core.rendezvous.foyer;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractFoyer<I extends RawInput, O extends RawOutput> {

    private final AbstractRendezvous<I, O> rendezvous;

    protected AbstractFoyer(AbstractRendezvous<I, O> rendezvous) {
        this.rendezvous = rendezvous;
    }

    protected CompletableFuture<O> enter(I rawInput) {
        return rendezvous.encounter(rawInput);
    }
}
