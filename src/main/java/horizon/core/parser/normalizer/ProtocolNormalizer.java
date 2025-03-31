package horizon.core.parser.normalizer;

import horizon.core.input.RawInput;

public interface ProtocolNormalizer<T extends RawInput> {

    NormalizedInput normalize(T rawInput);

}
