package horizon.core.flow.normalizer;

import horizon.core.model.input.RawInput;

interface ProtocolNormalizer<T extends RawInput> {

    NormalizedInput normalize(T rawInput);

}
