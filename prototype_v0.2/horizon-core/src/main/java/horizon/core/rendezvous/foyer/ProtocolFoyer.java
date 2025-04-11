package horizon.core.rendezvous.foyer;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.concurrent.CompletableFuture;

public interface ProtocolFoyer<I extends RawInput, O extends RawOutput> {

    CompletableFuture<O> enter(I rawInput);
}
