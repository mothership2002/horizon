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
     * Constructs a new HorizonNettyBootstrap instance using the provided Netty engine context.
     *
     * <p>This constructor initializes the bootstrap by passing the given context to the superclass.
     * The context encapsulates Netty-specific configurations and resources required for the server.</p>
     *
     * @param nettyEngineContext the engine context containing Netty-related settings
     */
    public HorizonNettyBootstrap(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> nettyEngineContext) {
        super(nettyEngineContext);
    }

    /**
     * Starts the Netty server by initializing event loop groups, configuring the server bootstrap,
     * and binding to the port specified in the context's properties.
     *
     * <p>This method waits until the server channel is closed before shutting down the event loop groups gracefully.</p>
     *
     * @param context the horizon context containing Netty configuration properties (such as port and thread sizes)
     * @throws Exception if an error occurs during server startup or binding operations
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