package horizon.core.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.concurrent.CompletableFuture;

interface Rendezvous<I extends RawInput, O extends RawOutput> {

    CompletableFuture<O> encounter(I rawInput);
}
