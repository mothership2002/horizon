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
     * Initializes the channel pipeline for the provided socket channel.
     *
     * <p>This method configures the channel by adding:
     * <ol>
     *   <li>An {@code IdleStateHandler} to manage idle timeouts, configured using the Netty properties.</li>
     *   <li>An {@code HttpServerCodec} for HTTP message encoding and decoding.</li>
     *   <li>An {@code HttpObjectAggregator} to aggregate HTTP messages into a complete request or response.</li>
     *   <li>A {@code HorizonNettyAdapter} for protocol-specific message handling.</li>
     * </ol>
     * The properties used for configuration are obtained from the context's {@code NettyProperties}.</p>
     *
     * @param ch the socket channel whose pipeline is to be initialized
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
     * Returns a new HttpObjectAggregator instance configured to aggregate HTTP messages up to the maximum content length specified in the Netty properties.
     *
     * @param nettyContext the Netty configuration properties containing the maximum content length
     * @return a new HttpObjectAggregator instance set with the specified maximum content length
     */
    private HttpObjectAggregator getHttpObjectAggregator(NettyProperties nettyContext) {
        return new HttpObjectAggregator(nettyContext.getMaxContentLength());
    }

    /**
     * Creates an IdleStateHandler configured with read, write, and all idle timeout values
     * derived from the provided NettyProperties. The timeout values are converted from milliseconds to seconds.
     *
     * @param nettyContext the configuration properties containing the idle timeout values in milliseconds
     * @return a new IdleStateHandler instance configured with the specified timeouts in seconds
     */
    private IdleStateHandler getIdleStateHandler(NettyProperties nettyContext) {
        int readTimeoutSeconds = nettyContext.getReadTimeoutMillis() / 1000;
        int writeTimeoutSeconds = nettyContext.getWriteTimeoutMillis() / 1000;
        int allIdleTimeSeconds = nettyContext.getAllIdleTimeMillis() / 1000;
        return new IdleStateHandler(readTimeoutSeconds, writeTimeoutSeconds, allIdleTimeSeconds);
    }

    /**
     * Creates a new HttpServerCodec instance configured with the maximum initial line length, header size,
     * and chunk size derived from the provided NettyProperties.
     *
     * @param nettyContext the configuration properties that supply codec limits
     * @return a configured HttpServerCodec instance
     */
    private HttpServerCodec getHttpServerCodec(NettyProperties nettyContext) {
        int maxInitialLineLength = nettyContext.getMaxInitialLineLength();
        int maxHeaderSize = nettyContext.getMaxHeaderSize();
        int maxChunkSize = nettyContext.getMaxChunkSize();
        return new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }
}
