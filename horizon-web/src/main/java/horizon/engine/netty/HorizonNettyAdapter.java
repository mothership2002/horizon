package horizon.engine.netty;

import horizon.core.flow.parser.foyer.ProtocolFoyer;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HorizonNettyAdapter.class);

    private final ProtocolFoyer<NettyHttpRawInput> conductor;

    public HorizonNettyAdapter(ProtocolFoyer<NettyHttpRawInput> processor) {
        this.conductor = processor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        try {
            NettyHttpRawInput rawInput = new NettyHttpRawInput(request, ctx);
            NettyHttpRawOutput response = (NettyHttpRawOutput) conductor.process(rawInput);
            ctx.writeAndFlush(response.toNettyResponse()).addListener(future ->
                ctx.close()
            );

        } catch (Exception e) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            logger.error("", e);
        }
    }
}
