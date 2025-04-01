package horizon.core.flow.parser.pipeline;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface ProtocolPipeline<T extends RawInput, S extends RawOutput> {

    S handle(T rawInput);
}
