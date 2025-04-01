package horizon.core.context;

import horizon.core.flow.broker.BrokerManager;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.flow.parser.interpreter.ProtocolInterpreter;
import horizon.core.model.output.RawOutput;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;

import horizon.core.flow.parser.conductor.ProtocolConductor;

public interface HorizonContext<T extends RawInput, S extends RawOutput> {

    ProtocolNormalizer<T> provideNormalizer();

    ProtocolInterpreter provideInterpreter();

    BrokerManager provideBrokerManager();

    ServerEngine.ServerEngineTemplate<T, S> provideEngine();

    ProtocolConductor<T> provideProcessor();

    RawOutputBuilder<S> provideOutputBuilder();
}
