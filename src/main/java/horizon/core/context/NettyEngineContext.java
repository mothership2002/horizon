package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.model.input.RawInput;
import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.http.HttpRawOutput;
import horizon.core.parser.conductor.ProtocolConductor;
import horizon.core.parser.normalizer.ProtocolNormalizer;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.pipeline.SentinelInterface;
import horizon.engine.ServerEngine;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.engine.netty.interpreter.NettyInterpreter;
import horizon.engine.netty.parser.conductor.NettyConductor;
import horizon.engine.netty.parser.normalizer.NettyNormalizer;

import java.util.List;
import java.util.Set;

import static horizon.core.util.SentinelScanner.scanInbound;
import static horizon.core.util.SentinelScanner.scanOutbound;

public class NettyEngineContext extends AbstractHorizonContext<HttpRawInput, HttpRawOutput> {

    @Override
    public ProtocolNormalizer<HttpRawInput> provideNormalizer() {
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
    public ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap();
    }

    @Override
    public ProtocolConductor<HttpRawInput> provideProcessor() {
        return new NettyConductor<>(initializePipeline());
    }

    @Override
    public ProtocolPipeline<HttpRawInput, HttpRawOutput> initializePipeline() {
        ProtocolPipeline<HttpRawInput, HttpRawOutput> pipeline = new ProtocolPipeline<>(provideNormalizer(), provideInterpreter(), provideBrokerManager());
        scanInboundSentinels().forEach(pipeline::addInboundSentinel);
        scanOutboundSentinels().forEach(pipeline::addOutboundSentinel);
        return pipeline;
    }

    @Override
    protected List<SentinelInterface.InboundSentinel<HttpRawInput>> scanInboundSentinels() {
        return scanInbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }

    @Override
    protected List<SentinelInterface.OutboundSentinel<HttpRawOutput>> scanOutboundSentinels() {
        return scanOutbound(Set.of(RawInput.Scheme.http, RawInput.Scheme.https));
    }
}