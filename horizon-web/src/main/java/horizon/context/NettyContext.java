package horizon.context;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.Properties;
import horizon.core.context.ServerEngine;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

public class NettyContext extends AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> {


    public NettyContext(
            AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> protocolContext,
            AbstractPresentationContext presentationContext,
            AbstractExecutionContext executionContext,
            Properties nettyProperties
    ) {
        super(protocolContext, presentationContext, executionContext, nettyProperties);
    }

    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap(this);
    }


}