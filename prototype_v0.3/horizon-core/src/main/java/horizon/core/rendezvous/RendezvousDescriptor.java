package horizon.core.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

public record RendezvousDescriptor<I extends RawInput, N, K, P, O extends RawOutput> (String scheme,
                                                                                     AbstractRendezvous<I, N, K, P, O> rendezvous,
                                                                                     Class<I> inputType,
                                                                                     Class<O> outputType) {
}