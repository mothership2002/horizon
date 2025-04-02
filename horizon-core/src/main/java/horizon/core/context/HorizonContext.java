package horizon.core.context;

import horizon.core.conductor.ConductorManager;
import horizon.core.flow.parser.foyer.ProtocolFoyer;
import horizon.core.flow.parser.interpreter.ProtocolInterpreter;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface HorizonContext<T extends RawInput, S extends RawOutput> {

    ProtocolNormalizer<T> provideNormalizer();

    ProtocolInterpreter provideInterpreter();

    ConductorManager provideBrokerManager();

    ServerEngine.ServerEngineTemplate<T, S> provideEngine();

    ProtocolFoyer<T> provideFoyer();

    RawOutputBuilder<S> provideOutputBuilder();
}
