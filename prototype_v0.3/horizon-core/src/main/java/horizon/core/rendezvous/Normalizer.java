package horizon.core.rendezvous;

import horizon.core.model.RawInput;

public interface Normalizer<I extends RawInput, N> {
    N normalize(I input);
}
