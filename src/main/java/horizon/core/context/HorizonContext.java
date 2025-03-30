package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.input.RawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.parser.ProtocolNormalizer;

import horizon.engine.ServerEngineTemplate;

public interface HorizonContext<T extends RawInput> {

    ProtocolNormalizer<T> provideNormalizer();
    ProtocolInterpreter provideInterpreter();
    BrokerManager provideBrokerManager();
    ServerEngineTemplate<T> provideEngine();
}
