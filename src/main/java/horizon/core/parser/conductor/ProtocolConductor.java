package horizon.core.parser.conductor;

import horizon.core.input.RawInput;
import horizon.core.input.http.HttpRawInput;
import horizon.core.output.RawOutput;

public interface ProtocolConductor<T extends RawInput> {
    RawOutput process(T rawInput);

    interface HttpConductor<T extends HttpRawInput> extends ProtocolConductor<T> {
    }

}
