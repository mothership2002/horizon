package horizon.context;

import horizon.core.constant.Scheme;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.Properties;
import horizon.core.context.ServerEngine;
import horizon.core.util.SentinelScanner;
import horizon.engine.netty.HorizonNettyBootstrap;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;

public class NettyContext extends AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> {


    /**
     * Constructs a new NettyContext with the specified contexts and Netty configuration.
     *
     * <p>This constructor delegates initialization to the superclass using the provided protocol,
     * presentation, and execution contexts along with the Netty properties. It also accepts a
     * communication scheme and a sentinel scanner to configure additional server capabilities.
     *
     * @param scheme the communication scheme (e.g., HTTP, HTTPS) used by the server
     * @param scanner the sentinel scanner responsible for managing alert conditions
     */
    public NettyContext(
            AbstractProtocolContext<NettyHttpRawInput, NettyHttpRawOutput> protocolContext,
            AbstractPresentationContext presentationContext,
            AbstractExecutionContext executionContext,
            Properties nettyProperties,
            Scheme scheme,
            SentinelScanner scanner
    ) {
        super(protocolContext, presentationContext, executionContext, nettyProperties, scheme, scanner);
    }

    /**
     * Provides a Netty server engine.
     *
     * <p>This method creates a new {@link HorizonNettyBootstrap} that implements
     * {@link ServerEngine.ServerEngineTemplate} for handling {@code NettyHttpRawInput} and {@code NettyHttpRawOutput}.
     * The returned engine uses the current context to bootstrap and configure the Netty-based server.</p>
     *
     * @return a new instance of the Netty server engine
     */
    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap(this);
    }



}