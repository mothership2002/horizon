package horizon.core.rendezvous.netty;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.Rendezvous;
import horizon.core.rendezvous.protocol.ProtocolFoyer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the ProtocolFoyer for Netty HTTP.
 * This class handles the Netty-specific server setup and message handling for HTTP.
 *
 * @param <I> the type of raw input this foyer can handle
 * @param <O> the type of raw output this foyer produces
 */
public class NettyHttpFoyer<I extends RawInput, O extends RawOutput> 
        extends ProtocolFoyer<I, O, FullHttpRequest, FullHttpResponse> {
    
    private static final Logger LOGGER = Logger.getLogger(NettyHttpFoyer.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(
            new DefaultThreadFactory("netty-http-handler"));
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /**
     * Creates a new NettyHttpFoyer with the specified port, rendezvous, and adapter.
     *
     * @param port the port to listen on, or 0 to use the protocol's default port
     * @param rendezvous the rendezvous to pass requests to
     * @param adapter the adapter to convert between HTTP requests/responses and Horizon's RawInput/RawOutput
     * @throws NullPointerException if rendezvous or adapter is null
     * @throws IllegalArgumentException if port is invalid
     */
    public NettyHttpFoyer(int port, Rendezvous<I, O> rendezvous, NettyHttpAdapter<I, O> adapter) {
        super(port, rendezvous, adapter, new NettyHttpProtocol());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initializeServer() throws Exception {
        LOGGER.info("Initializing Netty HTTP server on port " + getPort());
        
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("netty-http-boss"));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("netty-http-worker"));
        
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new NettyHttpHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        
        serverChannel = bootstrap.bind(getPort()).sync().channel();
        LOGGER.info("Netty HTTP server initialized and listening on port " + getPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutdownServer() throws Exception {
        LOGGER.info("Shutting down Netty HTTP server");
        
        if (serverChannel != null) {
            serverChannel.close().sync();
            serverChannel = null;
        }
        
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        
        LOGGER.info("Netty HTTP server shut down successfully");
    }

    /**
     * Netty channel handler for HTTP requests.
     */
    private class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            // If this is not a keep-alive connection, close it after sending the response
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            
            // Get the client's IP address
            String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
            
            // Process the request asynchronously to avoid blocking the Netty event loop
            CompletableFuture.supplyAsync(() -> {
                // Handle the message using the ProtocolFoyer's handleMessage method
                return handleMessage(request, remoteAddress, keepAlive);
            }, EXECUTOR).thenAccept(response -> {
                // Send the response back to the client
                ctx.writeAndFlush(response);
                
                // If this is not a keep-alive connection, close it after sending the response
                if (!keepAlive) {
                    ctx.close();
                }
            }).exceptionally(e -> {
                LOGGER.log(Level.SEVERE, "Error sending response: " + e.getMessage(), e);
                ctx.close();
                return null;
            });
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.log(Level.SEVERE, "Channel exception: " + cause.getMessage(), cause);
            ctx.close();
        }
    }
}