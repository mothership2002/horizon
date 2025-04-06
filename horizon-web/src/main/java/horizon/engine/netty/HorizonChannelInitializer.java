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

    protected void initChannel(SocketChannel ch) {
        NettyProperties nettyContext = (NettyProperties) context.getProperties();

        ch.pipeline()
                .addLast(getIdleStateHandler(nettyContext))
                .addLast(getHttpServerCodec(nettyContext))
                .addLast(getHttpObjectAggregator(nettyContext))
                .addLast(new HorizonNettyAdapter(context.protocolContext().provideFoyer()));
    }

    private HttpObjectAggregator getHttpObjectAggregator(NettyProperties nettyContext) {
        return new HttpObjectAggregator(nettyContext.getMaxContentLength());
    }

    private IdleStateHandler getIdleStateHandler(NettyProperties nettyContext) {
        int readTimeoutSeconds = nettyContext.getReadTimeoutMillis() / 1000;
        int writeTimeoutSeconds = nettyContext.getWriteTimeoutMillis() / 1000;
        int allIdleTimeSeconds = nettyContext.getAllIdleTimeMillis() / 1000;
        return new IdleStateHandler(readTimeoutSeconds, writeTimeoutSeconds, allIdleTimeSeconds);
    }

    private HttpServerCodec getHttpServerCodec(NettyProperties nettyContext) {
        int maxInitialLineLength = nettyContext.getMaxInitialLineLength();
        int maxHeaderSize = nettyContext.getMaxHeaderSize();
        int maxChunkSize = nettyContext.getMaxChunkSize();
        return new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }
}
