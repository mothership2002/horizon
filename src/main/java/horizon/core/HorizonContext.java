package horizon.core;

import horizon.core.broker.BrokerManager;
import horizon.core.input.RawInput;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.parser.ProtocolNormalizer;
import horizon.engine.netty.interpreter.NettyInterpreter;
import horizon.engine.netty.parser.NettyNormalizer;

public class HorizonContext {

    public ProtocolNormalizer<RawInput> providerNormalizer() {
        return new NettyNormalizer<>();
    }

    public ProtocolInterpreter providerInterpreter() {
        return new NettyInterpreter();
    }

    public BrokerManager providerBrokerManager() {
        return new BrokerManager();
    }
}
