package horizon.flow.foyer;

import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.output.RawOutput;
import horizon.protocol.http.input.HttpRawInput;

public abstract class HttpFoyer<T extends HttpRawInput> extends AbstractProtocolFoyer<T> {

    public HttpFoyer(AbstractProtocolRendezvous<T, ? extends RawOutput> rendezvous) {
        super(rendezvous);
    }
}
