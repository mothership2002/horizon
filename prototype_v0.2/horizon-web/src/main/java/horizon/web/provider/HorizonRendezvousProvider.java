package horizon.web.provider;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.web.rendezvous.RendezvousBuilder;

public interface HorizonRendezvousProvider<I extends RawInput, O extends RawOutput> {

    AbstractRendezvous<I, O> build(RendezvousBuilder<I, O> builder);

    Scheme getScheme();
}
