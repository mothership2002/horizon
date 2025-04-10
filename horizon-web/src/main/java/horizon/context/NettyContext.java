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
     * Constructs a new NettyContext with the specified dependencies.
     *
     * <p>This context manages the configuration and operational settings for a Netty-based server.
     * It passes the provided protocol, presentation, and execution contexts, along with Netty properties,
     * network scheme, and sentinel scanner, to its superclass.
     *
     * @param protocolContext the protocol context handling Netty HTTP raw input/output
     * @param presentationContext the presentation context used for data formatting and processing
     * @param executionContext the execution context managing concurrent operations
     * @param nettyProperties the configuration properties for the Netty server
     * @param scheme the network communication scheme (e.g., HTTP or HTTPS)
     * @param scanner the utility used for scanning and validating sentinel configurations
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
     * Returns a new server engine instance for handling Netty HTTP raw input and output.
     *
     * This method creates a new {@link HorizonNettyBootstrap} using the current context,
     * which serves as the bootstrap for the Netty-based server engine.
     *
     * @return a new instance of {@link HorizonNettyBootstrap} configured with this context
     */
    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap(this);
    }



}