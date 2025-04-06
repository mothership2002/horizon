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
     * Configures the channel pipeline for a new socket channel.
     *
     * <p>This method retrieves Netty properties from the context and sets up the channel pipeline by adding:
     * an idle state handler (using configured timeout values), an HTTP server codec, and an HTTP object aggregator
     * (using the maximum content length). It then attaches a HorizonNettyAdapter initialized with the protocol context
     * to process HTTP requests.
     *
     * @param ch the socket channel to be initialized
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
     * Creates a new HttpObjectAggregator configured with the maximum HTTP content length.
     *
     * @param nettyContext the Netty properties instance used to determine the maximum content length for aggregation
     * @return a new HttpObjectAggregator set to aggregate HTTP messages up to the configured maximum content length
     */
    private HttpObjectAggregator getHttpObjectAggregator(NettyProperties nettyContext) {
        return new HttpObjectAggregator(nettyContext.getMaxContentLength());
    }

    /**
     * Creates an IdleStateHandler using idle timeout values from the provided Netty properties.
     *
     * <p>
     * This method converts the millisecond timeout values for read, write, and all idle events to seconds, and then
     * uses these values to configure the IdleStateHandler.
     * </p>
     *
     * @param nettyContext the Netty properties containing the timeout values in milliseconds
     * @return an IdleStateHandler configured with the read, write, and all idle timeouts in seconds
     */
    private IdleStateHandler getIdleStateHandler(NettyProperties nettyContext) {
        int readTimeoutSeconds = nettyContext.getReadTimeoutMillis() / 1000;
        int writeTimeoutSeconds = nettyContext.getWriteTimeoutMillis() / 1000;
        int allIdleTimeSeconds = nettyContext.getAllIdleTimeMillis() / 1000;
        return new IdleStateHandler(readTimeoutSeconds, writeTimeoutSeconds, allIdleTimeSeconds);
    }

    /**
     * Creates a new HTTP server codec configured with the maximum initial line length, header size, and chunk size from the given Netty properties.
     *
     * @param nettyContext the Netty properties providing configuration values for the HTTP server codec
     * @return a configured instance of HttpServerCodec
     */
    private HttpServerCodec getHttpServerCodec(NettyProperties nettyContext) {
        int maxInitialLineLength = nettyContext.getMaxInitialLineLength();
        int maxHeaderSize = nettyContext.getMaxHeaderSize();
        int maxChunkSize = nettyContext.getMaxChunkSize();
        return new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }
}
