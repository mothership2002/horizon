package horizon.context;

import horizon.core.flow.broker.BrokerManager;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.core.flow.parser.interpreter.ProtocolInterpreter;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.protocol.http.NettyRawOutputBuilder;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;
import horizon.core.flow.parser.conductor.ProtocolConductor;
import horizon.core.flow.parser.normalizer.ProtocolNormalizer;
import horizon.parser.pipeline.DefaultProtocolPipeline;
import horizon.core.flow.centinel.SentinelInterface;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.parser.conductor.NettyConductor;
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
    public BrokerManager provideBrokerManager() {
        return new BrokerManager();
    }

    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap();
    }

    @Override
    public ProtocolConductor<NettyHttpRawInput> provideProcessor() {
        return new NettyConductor<>(initializePipeline());
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
    protected List<SentinelInterface.InboundSentinel<NettyHttpRawInput>> scanInboundSentinels() {
        return scanInbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    protected List<SentinelInterface.OutboundSentinel<NettyHttpRawOutput>> scanOutboundSentinels() {
        return scanOutbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }
}