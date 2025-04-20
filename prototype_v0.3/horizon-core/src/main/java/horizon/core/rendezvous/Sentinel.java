package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;

public interface Sentinel<I extends RawInput> {

    void inspectInbound(I input) throws SecurityException;

    void inspectOutbound(HorizonContext context);
}