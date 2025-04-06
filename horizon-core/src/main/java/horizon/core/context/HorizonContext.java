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
 * Retrieves the protocol context that encapsulates protocol-specific functionalities.
 *
 * @return the protocol context instance managing operations such as output building and protocol foyer access
 */
ProtocolContext<I, O> protocolContext();

    ExecutionContext executionContext();

    PresentationContext presentationContext();

    ServerEngine.ServerEngineTemplate<I, O> provideEngine();

    Properties getProperties();

    interface ProtocolContext<I extends RawInput, O extends RawOutput> {
        RawOutputBuilder<O> provideOutputBuilder();

        /**
 * Returns the protocol foyer that encapsulates protocol-specific input handling logic.
 *
 * <p>This method provides the instance responsible for processing the raw input data (of type {@code I}) 
 * within the protocol context.</p>
 *
 * @return the protocol foyer instance
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
