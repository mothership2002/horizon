package horizon.core.flow.foyer;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolFoyer<T extends RawInput> {

    CompletableFuture<? extends RawOutput> enter(T rawInput);

}
