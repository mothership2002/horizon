package horizon.engine.netty;

import horizon.core.broker.BrokerManager;
import horizon.core.context.HorizonContext;
import horizon.core.input.http.HttpRawInput;
import horizon.core.input.http.netty.NettyHttpRawInput;
import horizon.core.interpreter.ParsedRequest;
import horizon.core.interpreter.ProtocolInterpreter;
import horizon.core.output.HorizonRawOutputBuilder;
import horizon.core.output.RawOutput;
import horizon.core.parser.NormalizedInput;
import horizon.core.parser.ProtocolNormalizer;
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

    private final ProtocolNormalizer<HttpRawInput> normalizer;
    private final ProtocolInterpreter interpreter;
    private final BrokerManager brokerManager;

    public HorizonNettyAdapter(HorizonContext<HttpRawInput> context) {
        this.normalizer = context.provideNormalizer();
        this.brokerManager = context.provideBrokerManager();
        this.interpreter = context.provideInterpreter();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        try {
            HttpRawInput rawInput = new NettyHttpRawInput(request, ctx);
            NormalizedInput normalized = normalizer.normalize(rawInput);
            ParsedRequest parsed = interpreter.interpret(normalized);
            Object result = brokerManager.handle(parsed);
            RawOutput response = HorizonRawOutputBuilder.build(result);
            ctx.writeAndFlush(response);

        } catch (Exception e) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR));
            logger.error("", e);
        }
    }
}
