package horizon.engine.netty;

import horizon.context.NettyProperties;
import horizon.core.context.AbstractHorizonContext;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

public class HorizonChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> context;

    public HorizonChannelInitializer(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> context) {
        this.context = context;
    }

    /**
     * Initializes the specified SocketChannel by configuring its pipeline with handlers for idle detection,
     * HTTP message processing, and protocol-specific adaptation.
     *
     * <p>This method retrieves the Netty properties from the context to create an IdleStateHandler, an HttpServerCodec,
     * and an HttpObjectAggregator via dedicated helper methods, and then adds a HorizonNettyAdapter to the pipeline
     * for further protocol handling.</p>
     *
     * @param ch the SocketChannel to initialize
     */
    protected void initChannel(SocketChannel ch) {
        NettyProperties nettyContext = (NettyProperties) context.getProperties();

        ch.pipeline()
                .addLast(getIdleStateHandler(nettyContext))
                .addLast(getHttpServerCodec(nettyContext))
                .addLast(getHttpObjectAggregator(nettyContext))
                .addLast(new HorizonNettyAdapter(context.protocolContext().provideFoyer()));
    }

    /**
     * Creates an HttpObjectAggregator using the maximum content length from the provided NettyProperties.
     *
     * @return a new HttpObjectAggregator instance configured with the maximum allowed content length.
     */
    private HttpObjectAggregator getHttpObjectAggregator(NettyProperties nettyContext) {
        return new HttpObjectAggregator(nettyContext.getMaxContentLength());
    }

    /**
     * Creates an IdleStateHandler configured with read, write, and all idle timeouts,
     * converting the timeout values from milliseconds to seconds.
     *
     * @param nettyContext the Netty properties containing the timeout values in milliseconds
     * @return an IdleStateHandler instance with the read, write, and all idle timeouts set (in seconds)
     */
    private IdleStateHandler getIdleStateHandler(NettyProperties nettyContext) {
        int readTimeoutSeconds = nettyContext.getReadTimeoutMillis() / 1000;
        int writeTimeoutSeconds = nettyContext.getWriteTimeoutMillis() / 1000;
        int allIdleTimeSeconds = nettyContext.getAllIdleTimeMillis() / 1000;
        return new IdleStateHandler(readTimeoutSeconds, writeTimeoutSeconds, allIdleTimeSeconds);
    }

    /**
     * Creates an HttpServerCodec instance using configuration values from the provided NettyProperties.
     *
     * <p>The returned codec is configured with the maximum initial line length, header size, and chunk size
     * as defined in the nettyContext.</p>
     *
     * @param nettyContext configuration properties for the Netty HTTP server
     * @return an HttpServerCodec configured with values from nettyContext
     */
    private HttpServerCodec getHttpServerCodec(NettyProperties nettyContext) {
        int maxInitialLineLength = nettyContext.getMaxInitialLineLength();
        int maxHeaderSize = nettyContext.getMaxHeaderSize();
        int maxChunkSize = nettyContext.getMaxChunkSize();
        return new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }
}
