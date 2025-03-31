package horizon.engine.netty;

import horizon.core.input.http.HttpRawInput;
import horizon.core.input.http.netty.NettyHttpRawInput;
import horizon.core.output.RawOutput;
import horizon.core.parser.conductor.ProtocolConductor;
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

    private final ProtocolConductor<HttpRawInput> processor;

    public HorizonNettyAdapter(ProtocolConductor<HttpRawInput> processor) {
        this.processor = processor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        try {
            HttpRawInput rawInput = new NettyHttpRawInput(request, ctx);
            RawOutput response = processor.process(rawInput);
            ctx.writeAndFlush(response);

        } catch (Exception e) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            logger.error("", e);
        }
    }
}
