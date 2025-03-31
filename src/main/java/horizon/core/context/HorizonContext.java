package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.model.input.RawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.normalizer.ProtocolNormalizer;

import horizon.core.parser.conductor.ProtocolConductor;
import horizon.engine.ServerEngine;

public interface HorizonContext<T extends RawInput, S extends RawOutput> {

    ProtocolNormalizer<T> provideNormalizer();
    ProtocolInterpreter provideInterpreter();
    BrokerManager provideBrokerManager();
    ServerEngine.ServerEngineTemplate<T, S> provideEngine();
    ProtocolConductor<T> provideProcessor();
}
