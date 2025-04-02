package horizon.engine.netty;

import horizon.context.NettyEngineContext;
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

    @Override
    protected void doStart(AbstractHorizonContext<NettyHttpRawInput, NettyHttpRawOutput> context) throws Exception {
        NettyEngineContext nettyEngineContext = (NettyEngineContext) context;
        int port = getPort(nettyEngineContext);

        int eventLoopGroupSize = nettyEngineContext.getEventLoopGroupSize();
        int workerThreadSize = nettyEngineContext.getWorkerThreadSize();

        NioEventLoopGroup boss = new NioEventLoopGroup(eventLoopGroupSize);
        NioEventLoopGroup worker = new NioEventLoopGroup(workerThreadSize);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HorizonChannelInitializer(nettyEngineContext));

            ChannelFuture future = serverBootstrap.bind(port).sync();
            logger.info("Horizon Engine : Netty , listening on port : {}", port);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private int getPort(NettyEngineContext nettyEngineContext) {
        return nettyEngineContext.getPort() == null ? 8080 : nettyEngineContext.getPort();
    }
}