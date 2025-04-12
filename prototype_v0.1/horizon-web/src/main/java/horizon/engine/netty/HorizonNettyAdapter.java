package horizon.engine.netty;

import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HorizonNettyAdapter.class);

    private final AbstractProtocolFoyer<NettyHttpRawInput> foyer;

    public HorizonNettyAdapter(AbstractProtocolFoyer<NettyHttpRawInput> foyer) {
        this.foyer = foyer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        NettyHttpRawInput rawInput = new NettyHttpRawInput(request, ctx);
        foyer.enter(rawInput).thenAccept(response -> {
            ctx.writeAndFlush(((NettyHttpRawOutput) response).toNettyResponse())
                    .addListener(future -> connection(future, keepAlive, ctx));
        }).exceptionally(ex -> {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            logger.error("Unhandled error in request processing", ex);
            return null;
        });
    }

    private void connection(Future<? super Void> future, boolean isKeepAlive, ChannelHandlerContext ctx) {
        if (!future.isSuccess()) {
            logger.error("Failed to send response ", future.cause());
        }
        if (!isKeepAlive) {
            ctx.close();
        }
    }
}
