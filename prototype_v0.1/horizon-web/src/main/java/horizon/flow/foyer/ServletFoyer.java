package horizon.flow.foyer;

import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.output.RawOutput;
import horizon.protocol.http.input.HttpRawInput;

import java.util.concurrent.CompletableFuture;

public class ServletFoyer<T extends HttpRawInput> extends HttpFoyer<T> {

    public ServletFoyer(AbstractProtocolRendezvous<T, RawOutput> rendezvous) {
        super(rendezvous);
    }

    @Override
    public CompletableFuture<? extends RawOutput> enter(T rawInput) {
//        return rendezvous.encounter(rawInput);
        return null;
    }
}
