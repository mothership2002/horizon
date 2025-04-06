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
 * Retrieves the protocol context associated with this horizon context.
 *
 * <p>The returned protocol context provides access to protocol-specific components,
 * such as the output builder and protocol foyer, necessary for processing raw inputs
 * and generating raw outputs.</p>
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
 * Returns the protocol foyer instance.
 *
 * <p>This method provides an instance of {@link AbstractProtocolFoyer} that encapsulates the logic
 * for initializing and processing protocol-specific input. The returned foyer serves as the entry
 * point for handling raw input data in the protocol context.</p>
 *
 * @return the protocol foyer for initializing and processing protocol operations
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
