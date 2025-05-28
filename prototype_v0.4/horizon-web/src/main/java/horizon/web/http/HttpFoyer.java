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
 * 
 * The HttpFoyer sets up a Netty-based HTTP server that listens for incoming HTTP requests,
 * forwards them to the Rendezvous for processing, and returns the responses to clients.
 * It handles the HTTP protocol-specific aspects of request and response handling.
 */
public class HttpFoyer extends AbstractWebFoyer<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpFoyer.class);

    /**
     * Creates a new HTTP Foyer that listens on the specified port.
     *
     * @param port the port to listen on
     */
    public HttpFoyer(int port) {
        super(port);
    }

    /**
     * Returns the name of this protocol for logging and identification purposes.
     *
     * @return the protocol name ("HTTP")
     */
    @Override
    protected String getProtocolName() {
        return "HTTP";
    }

    /**
     * Creates a Netty channel initializer for HTTP connections.
     * This sets up the HTTP processing pipeline with:
     * - HttpServerCodec for HTTP request/response encoding and decoding
     * - HttpObjectAggregator to combine HTTP message fragments
     * - HttpRequestHandler to process complete HTTP requests
     *
     * @return a channel initializer for HTTP connections
     */
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
     * This handler processes complete HTTP requests, forwards them to the Rendezvous
     * for intent resolution and processing, and returns the responses to clients.
     * It also handles error conditions and unexpected exceptions.
     */
    private class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        /**
         * Handles an HTTP request by forwarding it to the Rendezvous and returning the response.
         * This method is called by Netty when a complete HTTP request is received.
         *
         * @param ctx the channel handler context
         * @param request the HTTP request to process
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            logger.debug("Received HTTP request: {} {}", request.method(), request.uri());

            if (rendezvous == null) {
                logger.error("No rendezvous connected");
                sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
                return;
            }

            try {
                // Encounter at the rendezvous - forward the request for processing
                HorizonContext context = rendezvous.encounter(request);

                // Fall away with response - get the processed result
                FullHttpResponse response = (FullHttpResponse) rendezvous.fallAway(context);

                // Send response back to the client
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            } catch (Exception e) {
                logger.error("Error processing request", e);
                sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * Handles unexpected exceptions in the HTTP processing pipeline.
         * This method is called by Netty when an exception occurs during request processing.
         *
         * @param ctx the channel handler context
         * @param cause the exception that occurred
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Unexpected error in HTTP handler", cause);
            ctx.close();
        }

        /**
         * Sends an error response with the specified status code.
         * This is used when an error occurs during request processing.
         *
         * @param ctx the channel handler context
         * @param status the HTTP status code to send
         */
        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status
            );
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
