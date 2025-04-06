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
     * Constructs a new NettyProtocolContext with components necessary for managing Netty-based protocol interactions.
     *
     * <p>This constructor initializes the protocol context by creating a raw output builder and a protocol foyer using the provided
     * conductor manager, shadow stage, network scheme, and sentinel scanner. These components together support the handling of protocol output
     * and input within a Netty environment.</p>
     *
     * @param conductorManager the manager responsible for coordinating protocol operations
     * @param shadowStage the stage that maintains state related to protocol processing
     * @param scheme the configuration scheme for network communication
     * @param scanner the scanner used for monitoring protocol interactions
     */
    public NettyProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage,
                                Scheme scheme, SentinelScanner scanner) {

        super(createRawOutputBuilder(), createFoyer(conductorManager, shadowStage, scheme, scanner));
    }

    /**
     * Retrieves the RawOutputBuilder configured for Netty HTTP raw output.
     *
     * @return the raw output builder used to construct NettyHttpRawOutput instances
     */
    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return super.rawOutputBuilder;
    }

    /**
     * Returns the protocol foyer instance used for handling Netty HTTP raw input.
     *
     * <p>This method retrieves the pre-initialized foyer from the superclass, which manages
     * the processing of incoming Netty HTTP raw data.</p>
     *
     * @return the Netty protocol foyer for raw input processing
     */
    @Override
    public AbstractProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return super.foyer;
    }

    /**
     * Creates a new instance of NettyRawOutputBuilder.
     *
     * @return a newly created NettyRawOutputBuilder instance
     */
    private static NettyRawOutputBuilder createRawOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    /**
     * Creates a new instance of {@link NettyNormalizer}.
     *
     * @return a new {@link NettyNormalizer} instance
     */
    private static NettyNormalizer createNormalizer() {
        return new NettyNormalizer();
    }

    /**
     * Creates a new instance of NettyInterpreter.
     *
     * @return a new NettyInterpreter instance
     */
    private static NettyInterpreter createInterpreter() {
        return new NettyInterpreter();
    }

    /**
     * Creates a DefaultRendezvous instance configured for Netty HTTP raw input and output.
     *
     * <p>This method constructs the necessary protocol components—namely a normalizer, an interpreter,
     * and a raw output builder—and combines them with the provided conductor manager, shadow stage,
     * scheme, and sentinel scanner to form a rendezvous for processing Netty protocol operations.
     *
     * @param conductorManager manages conductor-related operations within the protocol context
     * @param shadowStage handles the shadow processing stage for protocol interactions
     * @param scheme defines the protocol scheme used in the configuration
     * @param scanner scans for sentinel events during rendezvous processing
     * @return a new DefaultRendezvous instance encapsulating the required Netty protocol components
     */
    private static DefaultRendezvous<NettyHttpRawInput, NettyHttpRawOutput> createRendezvous(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        NettyNormalizer normalizer = createNormalizer();
        NettyInterpreter interpreter = createInterpreter();
        NettyRawOutputBuilder outputBuilder = createRawOutputBuilder();
        return new DefaultRendezvous<>(normalizer, interpreter, conductorManager, outputBuilder, shadowStage, scheme, scanner);
    }

    /**
     * Creates a new NettyFoyer instance.
     *
     * <p>This method initializes a rendezvous using the provided conductor manager, shadow stage, protocol scheme, and sentinel scanner,
     * then wraps the result in a NettyFoyer for processing Netty HTTP raw input.</p>
     *
     * @param conductorManager the conductor manager coordinating protocol operations
     * @param shadowStage the shadow stage involved in protocol handling
     * @param scheme the protocol scheme defining context-specific behavior
     * @param scanner the sentinel scanner used for protocol verification
     * @return a NettyFoyer configured with a rendezvous built from the specified components
     */
    private static NettyFoyer<NettyHttpRawInput> createFoyer(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage, Scheme scheme, SentinelScanner scanner) {
        return new NettyFoyer<>(createRendezvous(conductorManager, shadowStage, scheme, scanner));
    }
}
