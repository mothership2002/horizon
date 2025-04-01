package horizon.core.flow.parser.interpreter;

import horizon.core.flow.parser.normalizer.NormalizedInput;

public interface ProtocolInterpreter {

    ParsedRequest interpret(NormalizedInput normalizedInput);

}
