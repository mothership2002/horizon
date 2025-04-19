package horizon.core.rendezvous;

import horizon.core.model.RawInput;

public interface Sentinel<I extends RawInput> {
    void inspect(I input) throws SecurityException;
}