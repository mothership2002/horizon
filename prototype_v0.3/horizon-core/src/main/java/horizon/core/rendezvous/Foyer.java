package horizon.core.rendezvous;

import horizon.core.model.RawInput;

public interface Foyer<I extends RawInput> {
    boolean allow(I input);
}
