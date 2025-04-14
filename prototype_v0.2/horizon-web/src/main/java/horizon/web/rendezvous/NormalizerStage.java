package horizon.web.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.normalizer.Normalizer;

public interface NormalizerStage<I extends RawInput, O extends RawOutput> {
    InterpreterStage<I, O> withNormalizer(Normalizer normalizer);
}
