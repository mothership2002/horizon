package horizon.parser.foyer;

import horizon.core.flow.parser.foyer.ProtocolFoyer;
import horizon.protocol.http.input.HttpRawInput;

public interface HttpFoyer<T extends HttpRawInput> extends ProtocolFoyer<T> {
}
