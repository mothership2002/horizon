package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

interface Rendezvous<I extends RawInput, O extends RawOutput> {

    HorizonContext encounter(I input);

    O fallAway(HorizonContext context);
}