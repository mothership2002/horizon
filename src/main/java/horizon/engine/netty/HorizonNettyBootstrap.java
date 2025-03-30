package horizon.engine.netty;

import horizon.core.HorizonChannelInitializer;
import horizon.engine.HorizonEngine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyBootstrap implements HorizonEngine {

    public static final Logger logger = LoggerFactory.getLogger(HorizonNettyBootstrap.class);

    public void start(int port) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HorizonChannelInitializer());

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Horizon Application initialize on port {}", port);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
