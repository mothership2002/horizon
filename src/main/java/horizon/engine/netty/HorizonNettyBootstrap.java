package horizon.engine.netty;

import horizon.core.context.HorizonContext;
import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.http.HttpRawOutput;
import horizon.engine.ServerEngine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorizonNettyBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    public static final Logger logger = LoggerFactory.getLogger(HorizonNettyBootstrap.class);

    @Override
    protected void doStart(HorizonContext<HttpRawInput, HttpRawOutput> context, int port) throws Exception {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HorizonChannelInitializer(context));

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Horizon Engine : Netty , listening on port : {}", port);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}