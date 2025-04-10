package horizon.core.context;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.event.AbstractEventHorizon;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractCentralStage;
import horizon.core.stage.AbstractShadowStage;

interface HorizonContext<I extends RawInput, O extends RawOutput> {

    /**
 * Returns the protocol context providing access to protocol-related functionalities.
 *
 * <p>This context is parameterized with input and output types, offering operations such as output construction and protocol initialization.</p>
 *
 * @return the protocol context instance
 */
ProtocolContext<I, O> protocolContext();

    ExecutionContext executionContext();

    PresentationContext presentationContext();

    ServerEngine.ServerEngineTemplate<I, O> provideEngine();

    Properties getProperties();

    interface ProtocolContext<I extends RawInput, O extends RawOutput> {
        RawOutputBuilder<O> provideOutputBuilder();

        /**
 * Retrieves the protocol foyer that handles the initial stage of protocol processing.
 *
 * @return the protocol foyer instance responsible for managing early protocol interactions.
 */
AbstractProtocolFoyer<I> provideFoyer();

    }

    interface PresentationContext {
        AbstractConductorManager provideConductorManager();
    }

    interface ExecutionContext {
        AbstractEventHorizon provideEventHorizon();

        AbstractShadowStage provideShadowStage();

        AbstractCentralStage provideCentralStage();
    }
}
