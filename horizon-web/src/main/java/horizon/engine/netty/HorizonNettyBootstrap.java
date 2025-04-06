package horizon.engine.netty;

import horizon.context.NettyProperties;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.protocol.http.input.netty.NettyHttpRawInput;
import horizon.protocol.http.output.netty.NettyHttpRawOutput;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyBootstrap extends ServerEngine.ServerEngineTemplate<NettyHttpRawInput, NettyHttpRawOutput> {

    public static final Logger logger = LoggerFactory.getLogger(HorizonNettyBootstrap.class);

    /**
     * Constructs a HorizonNettyBootstrap instance with the specified Netty engine context.
     *
     * <p>The provided context contains the configuration and utilities required to initialize the Netty-based server,
     * and is passed to the superclass for further processing.
     *
     * @param nettyEngineContext the context for Netty server configuration and initialization
     */
    public HorizonNettyBootstrap(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> nettyEngineContext) {
        super(nettyEngineContext);
    }

    /**
     * Starts the Netty server using the provided context.
     *
     * <p>This method retrieves the Netty server configuration from the context, including event loop group sizes and
     * the port number, then initializes the boss and worker event loop groups. It configures the server bootstrap with a
     * channel initializer, binds to the specified port, and blocks until the server channel is closed. In all cases, it
     * gracefully shuts down the event loop groups.</p>
     *
     * @param context the server context containing the Netty configuration properties
     * @throws Exception if an error occurs during server startup or while waiting for the channel to close
     */
    @Override
    protected void doStart(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> context) throws Exception {
        NettyProperties props = (NettyProperties) context.getProperties();

        NioEventLoopGroup boss = new NioEventLoopGroup(props.getEventLoopGroupSize());
        NioEventLoopGroup worker = new NioEventLoopGroup(props.getWorkerThreadSize());

        int port = props.getPort();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HorizonChannelInitializer(this.context));

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Horizon Engine : Netty , listening on port : {}", port);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


}