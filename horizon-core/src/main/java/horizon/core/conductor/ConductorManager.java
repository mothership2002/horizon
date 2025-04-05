package horizon.core.conductor;

import horizon.core.flow.interpreter.ParsedRequest;

interface ConductorManager {

    Object conduct(ParsedRequest request);
}
