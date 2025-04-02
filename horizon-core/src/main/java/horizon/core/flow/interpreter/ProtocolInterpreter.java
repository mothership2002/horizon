package horizon.core.flow.interpreter;

import horizon.core.flow.normalizer.NormalizedInput;

interface ProtocolInterpreter {

    ParsedRequest interpret(NormalizedInput normalizedInput);

}
