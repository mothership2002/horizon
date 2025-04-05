package horizon.context;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.stage.AbstractShadowStage;
import horizon.flow.foyer.NettyFoyer;
import horizon.flow.interpreter.NettyInterpreter;
import horizon.flow.normalizer.NettyNormalizer;
import horizon.flow.rendezvous.DefaultRendezvous;
import horizon.protocol.http.NettyRawOutputBuilder;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

import java.util.List;
import java.util.Set;

import static horizon.core.util.SentinelScanner.scanInbound;
import static horizon.core.util.SentinelScanner.scanOutbound;

public class NettyProtocolContext extends AbstractHorizonContext.AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> {

    public NettyProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage) {
        super(conductorManager, shadowStage);
    }

    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    @Override
    public AbstractProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return new NettyFoyer<>(provideRendezvous());
    }

    @Override
    public AbstractProtocolRendezvous<NettyHttpRawInput, NettyHttpRawOutput> provideRendezvous() {
        AbstractProtocolRendezvous<NettyHttpRawInput, NettyHttpRawOutput> rendezvous
                = new DefaultRendezvous<>(provideNormalizer(), provideInterpreter(),
                super.conductorManager, provideOutputBuilder(), super.shadowStage);
        scanInboundSentinels().forEach(rendezvous::addInboundSentinel);
        scanOutboundSentinels().forEach(rendezvous::addOutboundSentinel);
        return rendezvous;
    }

    @Override
    public List<FlowSentinelInterface.InboundSentinel<NettyHttpRawInput>> scanInboundSentinels() {
        return scanInbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    public List<FlowSentinelInterface.OutboundSentinel<NettyHttpRawOutput>> scanOutboundSentinels() {
        return scanOutbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    public AbstractProtocolNormalizer<NettyHttpRawInput> provideNormalizer() {
        return new NettyNormalizer();
    }

    @Override
    public AbstractProtocolInterpreter provideInterpreter() {
        return new NettyInterpreter();
    }
}
