package horizon.web.provider;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.scanner.SentinelScanner;

public interface HorizonRendezvousProvider<I extends RawInput, O extends RawOutput> {

    AbstractRendezvous<I, O> provide(SentinelScanner<I, O> scanner);
}
