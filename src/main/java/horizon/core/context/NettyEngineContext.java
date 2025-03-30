package horizon.core.context;

import horizon.core.broker.BrokerManager;
import horizon.core.input.http.HttpRawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.parser.ProtocolNormalizer;
import horizon.engine.ServerEngineTemplate;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.engine.netty.interpreter.NettyInterpreter;
import horizon.engine.netty.parser.NettyNormalizer;

public class NettyEngineContext implements HorizonContext<HttpRawInput> {

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
    public ServerEngineTemplate<HttpRawInput> provideEngine() {
        return new HorizonNettyBootstrap();
    }
}