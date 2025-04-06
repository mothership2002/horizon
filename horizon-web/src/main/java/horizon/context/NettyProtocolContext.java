package horizon.context;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.constant.Scheme;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.stage.AbstractShadowStage;
import horizon.core.util.SentinelScanner;
import horizon.flow.foyer.NettyFoyer;
import horizon.flow.interpreter.NettyInterpreter;
import horizon.flow.normalizer.NettyNormalizer;
import horizon.flow.rendezvous.DefaultRendezvous;
import horizon.protocol.http.NettyRawOutputBuilder;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

public class NettyProtocolContext extends AbstractHorizonContext.AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> {

    /**
     * Constructs a new NettyProtocolContext with the specified configuration parameters.
     * <p>
     * This constructor initializes the protocol context by creating a raw output builder and a foyer.
     * The raw output builder and foyer are configured using the given conductor manager, shadow stage,
     * protocol scheme, and sentinel scanner.
     * </p>
     *
     * @param conductorManager the manager responsible for protocol orchestration
     * @param shadowStage the stage managing shadow operations for protocol handling
     * @param scheme the protocol scheme configuration
     * @param scanner the sentinel scanner used for detecting protocol-specific signals
     */
    public NettyProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage,
                                Scheme scheme, SentinelScanner scanner) {

        super(createRawOutputBuilder(), createFoyer(conductorManager, shadowStage, scheme, scanner));
    }

    /**
     * Returns the raw output builder for Netty HTTP outputs.
     *
     * <p>This method retrieves the pre-configured output builder instance from the superclass,
     * which is used to generate raw outputs specific to Netty.
     *
     * @return the raw output builder instance for Netty HTTP raw outputs
     */
    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return super.rawOutputBuilder;
    }

    /**
     * Returns the protocol foyer responsible for handling Netty HTTP raw input.
     *
     * @return the protocol foyer instance for Netty HTTP raw input.
     */
    @Override
    public AbstractProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return super.foyer;
    }

    /**
     * Creates a new instance of NettyRawOutputBuilder.
     *
     * @return a new NettyRawOutputBuilder instance
     */
    private static NettyRawOutputBuilder createRawOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    /**
     * Creates a new instance of NettyNormalizer.
     *
     * @return a new NettyNormalizer instance
     */
    private static NettyNormalizer createNormalizer() {
        return new NettyNormalizer();
    }

    /**
     * Creates and returns a new {@link NettyInterpreter} instance.
     *
     * @return a new {@link NettyInterpreter} object.
     */
    private static NettyInterpreter createInterpreter() {
        return new NettyInterpreter();
    }

    /**
     * Creates a new rendezvous instance for processing Netty HTTP raw input and output.
     *
     * <p>This method instantiates the necessary components—a NettyNormalizer, a NettyInterpreter,
     * and a NettyRawOutputBuilder—and uses them in conjunction with the provided conductor manager,
     * shadow stage, scheme, and sentinel scanner to construct a DefaultRendezvous instance.</p>
     *
     * @return the configured DefaultRendezvous for handling Netty HTTP raw protocols
     */
    private static DefaultRendezvous<NettyHttpRawInput, NettyHttpRawOutput> createRendezvous(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        NettyNormalizer normalizer = createNormalizer();
        NettyInterpreter interpreter = createInterpreter();
        NettyRawOutputBuilder outputBuilder = createRawOutputBuilder();
        return new DefaultRendezvous<>(normalizer, interpreter, conductorManager, outputBuilder, shadowStage, scheme, scanner);
    }

    /**
     * Creates a new NettyFoyer instance for handling Netty HTTP raw inputs.
     *
     * <p>This method builds a rendezvous instance using the provided conductor manager, shadow stage, scheme,
     * and sentinel scanner, and then encapsulates it within a NettyFoyer.</p>
     *
     * @param conductorManager the manager that orchestrates protocol conduction processes
     * @param shadowStage the stage managing shadow operations within the protocol workflow
     * @param scheme the protocol configuration scheme
     * @param scanner the sentinel scanner used for detecting flow anomalies
     * @return a configured NettyFoyer instance
     */
    private static NettyFoyer<NettyHttpRawInput> createFoyer(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        return new NettyFoyer<>(createRendezvous(conductorManager, shadowStage, scheme, scanner));
    }
}
