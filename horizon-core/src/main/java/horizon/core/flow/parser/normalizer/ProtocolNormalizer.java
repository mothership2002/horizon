package horizon.core.flow.parser.normalizer;

import horizon.core.model.input.RawInput;

public interface ProtocolNormalizer<T extends RawInput> {

    NormalizedInput normalize(T rawInput);

}
