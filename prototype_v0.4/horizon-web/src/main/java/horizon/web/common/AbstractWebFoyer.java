package horizon.web.common;

import horizon.core.Rendezvous;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for web foyers (HTTP and WebSocket) that use Netty.
 * This class provides common Netty-specific functionality for web foyers.
 *
 * @param <I> the protocol-specific input type
 */
public abstract class AbstractWebFoyer<I> extends AbstractFoyer<I> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractWebFoyer.class);

    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;
    protected Channel serverChannel;

    public AbstractWebFoyer(int port) {
        super(port);
    }

    @Override
    public void open() {
        if (isOpen.compareAndSet(false, true)) {
            logger.info("Opening {} Foyer on port {}", getProtocolName(), port);

            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(createChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

                serverChannel = bootstrap.bind(port).sync().channel();
                logger.info("{} Foyer opened successfully on port {}", getProtocolName(), port);

            } catch (Exception e) {
                logger.error("Failed to open {} Foyer", getProtocolName(), e);
                close();
                throw new RuntimeException("Failed to open " + getProtocolName() + " Foyer", e);
            }
        }
    }

    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            logger.info("Closing {} Foyer", getProtocolName());

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

            logger.info("{} Foyer closed", getProtocolName());
        }
    }

    /**
     * Creates a channel initializer for the specific protocol.
     */
    protected abstract io.netty.channel.ChannelInitializer<?> createChannelInitializer();
}
