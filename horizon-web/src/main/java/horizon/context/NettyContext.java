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
     * Constructs a NettyContext with the specified runtime contexts, Netty properties, scheme, and scanner.
     *
     * <p>This constructor initializes the Netty-based HTTP processing context by passing all provided
     * arguments to its superclass, thereby configuring the protocol, presentation, and execution contexts
     * along with Netty-specific settings.</p>
     *
     * @param protocolContext the protocol context providing HTTP raw input and output handling
     * @param presentationContext the presentation context managing serialization and deserialization
     * @param executionContext the execution context encapsulating execution-related configurations
     * @param nettyProperties configuration properties for Netty
     * @param scheme the network scheme defining protocol specifics
     * @param scanner the SentinelScanner for detecting and handling specific annotations or settings
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
     * Provides the Netty server engine template for the current context.
     *
     * <p>This method instantiates a new {@link HorizonNettyBootstrap} using the current
     * Netty context, which configures and bootstraps the Netty-based server engine.</p>
     *
     * @return a new instance of the Netty server engine template
     */
    @Override
    public ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> provideEngine() {
        return new HorizonNettyBootstrap(this);
    }



}