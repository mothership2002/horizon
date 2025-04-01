package horizon.core.flow.parser.conductor;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface ProtocolConductor<T extends RawInput> {

    RawOutput process(T rawInput);


}
