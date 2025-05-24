package horizon.http;

import horizon.core.Foyer;
import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP Foyer - the entry point for HTTP requests into the Horizon framework.
 */
public class HttpFoyer implements Foyer<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(HttpFoyer.class);

    private final int port;
    private final AtomicBoolean isOpen = new AtomicBoolean(false);
    private Rendezvous<FullHttpRequest, FullHttpResponse> rendezvous;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public HttpFoyer(int port) {
        this.port = port;
    }

    @Override
    public void open() {
        if (isOpen.compareAndSet(false, true)) {
            logger.info("Opening HTTP Foyer on port {}", port);

            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new HttpRequestHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

                serverChannel = bootstrap.bind(port).sync().channel();
                logger.info("HTTP Foyer opened successfully on port {}", port);

            } catch (Exception e) {
                logger.error("Failed to open HTTP Foyer", e);
                close();
                throw new RuntimeException("Failed to open HTTP Foyer", e);
            }
        }
    }

    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            logger.info("Closing HTTP Foyer");

            try {
                if (serverChannel != null) {
                    serverChannel.close().sync();
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while closing server channel", e);
                Thread.currentThread().interrupt();
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }

            logger.info("HTTP Foyer closed");
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void connectToRendezvous(Rendezvous<FullHttpRequest, ?> rendezvous) {
        this.rendezvous = (Rendezvous<FullHttpRequest, FullHttpResponse>) rendezvous;
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
                FullHttpResponse response = rendezvous.fallAway(context);

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
