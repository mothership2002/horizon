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
     * Constructs a new HorizonNettyBootstrap instance using the provided configuration context.
     *
     * <p>The given context supplies Netty-specific settings and is passed to the superclass for initialization.
     */
    public HorizonNettyBootstrap(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> nettyEngineContext) {
        super(nettyEngineContext);
    }

    /**
     * Starts the Netty-based HTTP server using configuration from the provided context.
     *
     * <p>This method creates event loop groups for accepting connections and processing requests based on the sizes
     * specified in the Netty properties obtained from the context. It configures the server bootstrap with a channel
     * initializer, binds to the configured port, and blocks until the server channel is closed. In every case, the
     * event loop groups are shut down gracefully to release resources.
     *
     * @param context the context containing Netty properties used for server configuration.
     * @throws Exception if an error occurs during the server startup process.
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