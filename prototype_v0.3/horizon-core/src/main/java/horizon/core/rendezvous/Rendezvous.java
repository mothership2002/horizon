package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;

interface Rendezvous<I extends RawInput, K, P> {
    void receive(I input, HorizonContext context);
}