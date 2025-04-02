package horizon.context;

import horizon.core.conductor.ConductorManager;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.core.flow.parser.interpreter.ProtocolInterpreter;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.protocol.http.NettyRawOutputBuilder;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.core.flow.parser.foyer.ProtocolFoyer;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;
import horizon.parser.pipeline.DefaultProtocolPipeline;
import horizon.core.flow.centinel.FlowSentinelInterface;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.parser.foyer.NettyFoyer;
import horizon.parser.interpreter.NettyInterpreter;
import horizon.parser.normalizer.NettyNormalizer;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

import java.util.List;
import java.util.Set;

import static horizon.core.util.SentinelScanner.scanInbound;
import static horizon.core.util.SentinelScanner.scanOutbound;

public class NettyEngineContext extends AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> {

    @Override
    public ProtocolNormalizer<NettyHttpRawInput> provideNormalizer() {
        return new NettyNormalizer();
    }

    @Override
    public ProtocolInterpreter provideInterpreter() {
        return new NettyInterpreter();
    }

    @Override
    public ConductorManager provideBrokerManager() {
        return new ConductorManager();
    }

    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap();
    }

    @Override
    public ProtocolFoyer<NettyHttpRawInput> provideFoyer() {
        return new NettyFoyer<>(initializePipeline());
    }

    @Override
    public RawOutputBuilder<NettyHttpRawOutput> provideOutputBuilder() {
        return new NettyRawOutputBuilder();
    }

    @Override
    protected DefaultProtocolPipeline<NettyHttpRawInput, NettyHttpRawOutput> initializePipeline() {
        DefaultProtocolPipeline<NettyHttpRawInput, NettyHttpRawOutput> pipeline
                = new DefaultProtocolPipeline<>(provideNormalizer(), provideInterpreter(), provideBrokerManager(), provideOutputBuilder());
        scanInboundSentinels().forEach(pipeline::addInboundSentinel);
        scanOutboundSentinels().forEach(pipeline::addOutboundSentinel);
        return pipeline;
    }

    @Override
    protected List<FlowSentinelInterface.InboundSentinel<NettyHttpRawInput>> scanInboundSentinels() {
        return scanInbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    protected List<FlowSentinelInterface.OutboundSentinel<NettyHttpRawOutput>> scanOutboundSentinels() {
        return scanOutbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }
}