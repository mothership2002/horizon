package horizon.core.context;

import horizon.core.conductor.ConductorManager;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

interface HorizonContext<T extends RawInput, S extends RawOutput> {

    AbstractProtocolNormalizer<T> provideNormalizer();

    AbstractProtocolInterpreter provideInterpreter();

    ConductorManager provideConductorManager();

    ServerEngine.ServerEngineTemplate<T, S> provideEngine();

    AbstractProtocolFoyer<T> provideFoyer();

    RawOutputBuilder<S> provideOutputBuilder();
}
