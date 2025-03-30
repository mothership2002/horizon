package horizon.core.parser;

import horizon.core.input.RawInput;

public interface ProtocolNormalizer<T extends RawInput> {

    NormalizedInput normalize(T rawInput);

}
