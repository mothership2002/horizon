package horizon.core.flow.parser.foyer;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface ProtocolFoyer<T extends RawInput> {

    RawOutput process(T rawInput);


}
