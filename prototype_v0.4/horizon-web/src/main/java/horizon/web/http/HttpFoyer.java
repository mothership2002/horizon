package horizon.web.http;

import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import horizon.web.common.AbstractWebFoyer;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP Foyer - the entry point for HTTP requests into the Horizon framework.
 * This class extends AbstractWebFoyer to provide HTTP-specific functionality.
 */
public class HttpFoyer extends AbstractWebFoyer<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpFoyer.class);

    public HttpFoyer(int port) {
        super(port);
    }

    @Override
    protected String getProtocolName() {
        return "HTTP";
    }

    @Override
    protected ChannelInitializer<?> createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new HttpRequestHandler());
            }
        };
    }

    /**
     * Handles incoming HTTP requests and passes them to the Rendezvous.
     */
    private class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            logger.debug("Received HTTP request: {} {}", request.method(), request.uri());

            if (rendezvous == null) {
                logger.error("No rendezvous connected");
                sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
                return;
            }

            try {
                // Encounter at the rendezvous
                HorizonContext context = rendezvous.encounter(request);

                // Fall away with response
                FullHttpResponse response = (FullHttpResponse) rendezvous.fallAway(context);

                // Send response
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            } catch (Exception e) {
                logger.error("Error processing request", e);
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Unexpected error in HTTP handler", cause);
            ctx.close();
        }

        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status
            );
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}