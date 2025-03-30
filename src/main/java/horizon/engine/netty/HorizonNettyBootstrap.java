package horizon.engine.netty;

import horizon.core.HorizonContext;
import horizon.engine.ServerEngineTemplate;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyBootstrap extends ServerEngineTemplate {

    public static final Logger logger = LoggerFactory.getLogger(HorizonNettyBootstrap.class);

    @Override
    protected void doStart(int port) throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        HorizonContext horizonContext = new HorizonContext();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HorizonChannelInitializer(horizonContext));

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Horizon Engine : Netty");
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
