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

import java.util.concurrent.ExecutorService;

public class NettyProtocolContext extends AbstractHorizonContext.AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> {

    public NettyProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage,
                                Scheme scheme, SentinelScanner scanner, ExecutorService rendezvousExecutor) {
        super(createRawOutputBuilder(), createFoyer(conductorManager, shadowStage, scheme, scanner, rendezvousExecutor));
    }

    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return super.rawOutputBuilder;
    }

    @Override
    public AbstractProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return super.foyer;
    }

    private static NettyRawOutputBuilder createRawOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    private static NettyNormalizer createNormalizer() {
        return new NettyNormalizer();
    }

    private static NettyInterpreter createInterpreter() {
        return new NettyInterpreter();
    }

    private static DefaultRendezvous<NettyHttpRawInput, NettyHttpRawOutput> createRendezvous(AbstractConductorManager conductorManager,
                                                                                             AbstractShadowStage shadowStage,
                                                                                             Scheme scheme,
                                                                                             SentinelScanner scanner,
                                                                                             ExecutorService rendezvousExecutor) {
        NettyNormalizer normalizer = createNormalizer();
        NettyInterpreter interpreter = createInterpreter();
        NettyRawOutputBuilder outputBuilder = createRawOutputBuilder();
        return new DefaultRendezvous<>(normalizer, interpreter, conductorManager, outputBuilder, shadowStage, scheme, scanner, rendezvousExecutor);
    }

    private static NettyFoyer<NettyHttpRawInput> createFoyer(AbstractConductorManager conductorManager,
                                                             AbstractShadowStage shadowStage,
                                                             Scheme scheme,
                                                             SentinelScanner scanner,
                                                             ExecutorService rendezvousExecutor) {
        return new NettyFoyer<>(createRendezvous(conductorManager, shadowStage, scheme, scanner, rendezvousExecutor));
    }
}
