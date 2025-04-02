package horizon.engine.netty;

import horizon.context.NettyEngineContext;
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
        NettyEngineContext nettyContext = (NettyEngineContext) context;
        int readTimeoutSeconds = nettyContext.getReadTimeoutMillis() / 1000;
        int writeTimeoutSeconds = nettyContext.getWriteTimeoutMillis() / 1000;
        int allIdleTimeSeconds = nettyContext.getAllIdleTimeMillis() / 1000;
        int maxContentLength = nettyContext.getMaxContentLength();
        int maxInitialLineLength = nettyContext.getMaxInitialLineLength();
        int maxHeaderSize = nettyContext.getMaxHeaderSize();
        int maxChunkSize = nettyContext.getMaxChunkSize();

        ch.pipeline()
                .addLast(new IdleStateHandler(readTimeoutSeconds, writeTimeoutSeconds, allIdleTimeSeconds))
                .addLast(new HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize))
                .addLast(new HttpObjectAggregator(maxContentLength))
                .addLast(new HorizonNettyAdapter(context.provideFoyer()));
    }
}
