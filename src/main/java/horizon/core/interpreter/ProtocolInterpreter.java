package horizon.core.interpreter;

import horizon.core.parser.NormalizedInput;

public interface ProtocolInterpreter {

    ParsedRequest interpret(NormalizedInput normalizedInput);

}
