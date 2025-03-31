package horizon.core.interpreter;

import horizon.core.parser.normalizer.NormalizedInput;

public interface ProtocolInterpreter {

    ParsedRequest interpret(NormalizedInput normalizedInput);

}
