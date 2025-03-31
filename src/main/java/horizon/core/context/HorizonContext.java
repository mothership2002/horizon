package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.input.RawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.parser.normalizer.ProtocolNormalizer;

import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.conductor.ProtocolConductor;
import horizon.engine.ServerEngine;

public interface HorizonContext<T extends RawInput> {

    ProtocolNormalizer<T> provideNormalizer();
    ProtocolInterpreter provideInterpreter();
    BrokerManager provideBrokerManager();
    ServerEngine.ServerEngineTemplate<T> provideEngine();
    ProtocolConductor<T> provideProcessor();
}
