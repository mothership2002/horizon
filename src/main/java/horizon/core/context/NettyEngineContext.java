package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.input.http.HttpRawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.parser.normalizer.ProtocolNormalizer;
import horizon.core.parser.pipeline.InboundSentinel;
import horizon.core.parser.pipeline.OutboundSentinel;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.engine.netty.parser.conductor.NettyConductor;
import horizon.core.parser.conductor.ProtocolConductor;
import horizon.engine.ServerEngine;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.engine.netty.interpreter.NettyInterpreter;
import horizon.engine.netty.parser.normalizer.NettyNormalizer;

import java.util.List;

public class NettyEngineContext extends AbstractHorizonContext<HttpRawInput> {

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
    public ServerEngine.ServerEngineTemplate<HttpRawInput> provideEngine() {
        return new HorizonNettyBootstrap();
    }

    @Override
    public ProtocolConductor<HttpRawInput> provideProcessor() {
        return new NettyConductor<>(initializePipeline());
    }

    @Override
    public ProtocolPipeline<HttpRawInput> initializePipeline() {
        ProtocolPipeline<HttpRawInput> pipeline = new ProtocolPipeline<>(provideNormalizer(), provideInterpreter(), provideBrokerManager());
        super.scanInboundSentinels().forEach(pipeline::addInboundSentinel);
        super.scanOutboundSentinels().forEach(pipeline::addOutboundSentinel);
        return pipeline;
    }
}