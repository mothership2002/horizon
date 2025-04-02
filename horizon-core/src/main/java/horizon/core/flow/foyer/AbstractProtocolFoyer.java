package horizon.core.flow.foyer;

import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public abstract class AbstractProtocolFoyer<T extends RawInput> implements ProtocolFoyer<T> {

    protected final AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous;

    protected AbstractProtocolFoyer(AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous) {
        this.rendezvous = rendezvous;
    }
}
