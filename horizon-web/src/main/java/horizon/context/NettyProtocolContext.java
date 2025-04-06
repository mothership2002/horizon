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
     * Constructs a new NettyProtocolContext with the specified configuration components.
     *
     * This constructor initializes the protocol context by creating a raw output builder and a foyer using
     * dedicated static factory methods. The provided conductor manager, shadow stage, scheme, and sentinel scanner
     * are used to configure the protocol for Netty-based operations.
     *
     * @param conductorManager the conductor manager handling orchestration logic
     * @param shadowStage the shadow stage managing auxiliary protocol operations
     * @param scheme the scheme specifying protocol configuration parameters
     * @param scanner the sentinel scanner used to detect and process protocol sentinels
     */
    public NettyProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage,
                                Scheme scheme, SentinelScanner scanner) {

        super(createRawOutputBuilder(), createFoyer(conductorManager, shadowStage, scheme, scanner));
    }

    /**
     * Returns the output builder used for constructing Netty HTTP raw responses.
     *
     * <p>This method retrieves the output builder instance inherited from the superclass,
     * which is responsible for generating raw outputs specific to Netty HTTP operations.
     * </p>
     *
     * @return the output builder for Netty HTTP raw output.
     */
    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return super.rawOutputBuilder;
    }

    /**
     * Returns the protocol foyer for handling Netty HTTP raw input.
     *
     * @return the configured protocol foyer for Netty HTTP raw input.
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
     * Instantiates and returns a new {@link NettyNormalizer}.
     *
     * @return a new instance of {@code NettyNormalizer}
     */
    private static NettyNormalizer createNormalizer() {
        return new NettyNormalizer();
    }

    /**
     * Creates and returns a new instance of NettyInterpreter.
     *
     * @return a new NettyInterpreter used for interpreting Netty protocol data.
     */
    private static NettyInterpreter createInterpreter() {
        return new NettyInterpreter();
    }

    /**
     * Constructs and returns a DefaultRendezvous instance for coordinating Netty HTTP raw input and output.
     *
     * This helper method initializes the necessary internal components—a NettyNormalizer, a NettyInterpreter,
     * and a NettyRawOutputBuilder—and combines them with the provided conductor manager, shadow stage,
     * scheme, and sentinel scanner to form a fully configured DefaultRendezvous.
     *
     * @param conductorManager the manager responsible for protocol orchestration
     * @param shadowStage the stage designated for handling protocol-related events
     * @param scheme the protocol scheme configuration (e.g., HTTP or HTTPS)
     * @param scanner the scanner used for identifying sentinel events during protocol handling
     * @return a DefaultRendezvous instance configured with the specified and internally created components
     */
    private static DefaultRendezvous<NettyHttpRawInput, NettyHttpRawOutput> createRendezvous(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        NettyNormalizer normalizer = createNormalizer();
        NettyInterpreter interpreter = createInterpreter();
        NettyRawOutputBuilder outputBuilder = createRawOutputBuilder();
        return new DefaultRendezvous<>(normalizer, interpreter, conductorManager, outputBuilder, shadowStage, scheme, scanner);
    }

    /**
     * Creates a new NettyFoyer instance using a rendezvous configured with the specified components.
     *
     * @param conductorManager the conductor manager coordinating protocol events
     * @param shadowStage the shadow stage that provides stage-specific behavior
     * @param scheme the protocol scheme defining communication parameters
     * @param scanner the sentinel scanner responsible for managing protocol sentinels
     * @return a configured NettyFoyer instance for handling Netty HTTP raw input
     */
    private static NettyFoyer<NettyHttpRawInput> createFoyer(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        return new NettyFoyer<>(createRendezvous(conductorManager, shadowStage, scheme, scanner));
    }
}
