package horizon.parser.conductor;

import horizon.core.flow.parser.conductor.ProtocolConductor;
import horizon.protocol.http.input.HttpRawInput;

public interface HttpConductor<T extends HttpRawInput> extends ProtocolConductor<T> {
}
