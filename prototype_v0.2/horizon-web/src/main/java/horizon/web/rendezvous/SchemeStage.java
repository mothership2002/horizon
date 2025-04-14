package horizon.web.rendezvous;

import horizon.core.constant.Scheme;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

public interface SchemeStage<I extends RawInput, O extends RawOutput> {
    NormalizerStage<I, O> withScheme(Scheme scheme);
}
