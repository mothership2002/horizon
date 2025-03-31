package horizon.core.parser.conductor;

import horizon.core.model.input.RawInput;
import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.RawOutput;

public interface ProtocolConductor<T extends RawInput> {
    RawOutput process(T rawInput);

    interface HttpConductor<T extends HttpRawInput> extends ProtocolConductor<T> {
    }

}
