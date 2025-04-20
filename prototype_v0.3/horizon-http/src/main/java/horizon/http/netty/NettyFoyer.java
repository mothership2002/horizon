package horizon.http.netty;

import horizon.core.model.RawInput;
import horizon.core.rendezvous.Foyer;
import horizon.core.rendezvous.Rendezvous;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Netty-based implementation of the Foyer interface.
 * This class acts as an adapter between Netty and the Rendezvous component,
 * allowing the Horizon framework to handle HTTP requests using Netty.
 *
 * @param <I> the type of raw input this foyer can handle
 */
public class NettyFoyer<I extends RawInput> implements Foyer<I> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyFoyer.class);

    private final int port;
    private final Rendezvous<I, ?> rendezvous;
    private final NettyInputConverter<I> inputConverter;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private boolean initialized = false;

    /**
     * Creates a new NettyFoyer with the specified port, rendezvous, and input converter.
     *
     * @param port the port to listen on
     * @param rendezvous the rendezvous to pass requests to
     * @param inputConverter the converter to convert Netty requests to raw input
     * @throws NullPointerException if rendezvous or inputConverter is null
     * @throws IllegalArgumentException if port is invalid
     */
    public NettyFoyer(int port, Rendezvous<I, ?> rendezvous, NettyInputConverter<I> inputConverter) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        this.port = port;
        this.rendezvous = Objects.requireNonNull(rendezvous, "rendezvous must not be null");
        this.inputConverter = Objects.requireNonNull(inputConverter, "inputConverter must not be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allow(I input) {
        // Default implementation allows all inputs
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        if (initialized) {
            LOGGER.warn("NettyFoyer is already initialized");
            return;
        }

        LOGGER.info("Initializing NettyFoyer on port " + port);
        
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            
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
                            pipeline.addLast(new NettyRequestHandler<>(NettyFoyer.this, rendezvous, inputConverter));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            serverChannel = bootstrap.bind(port).sync().channel();
            initialized = true;
            
            LOGGER.info("NettyFoyer initialized and listening on port " + port);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize NettyFoyer: " + e.getMessage(), e);
            shutdown();
            throw new RuntimeException("Failed to initialize NettyFoyer", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (!initialized) {
            LOGGER.warn("NettyFoyer is not initialized");
            return;
        }

        LOGGER.info("Shutting down NettyFoyer");
        
        try {
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
            
            initialized = false;
            LOGGER.info("NettyFoyer shut down successfully");
        } catch (Exception e) {
            LOGGER.error("Error shutting down NettyFoyer: " + e.getMessage(), e);
        }
    }

    /**
     * Returns whether this foyer is initialized.
     *
     * @return true if this foyer is initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the port this foyer is listening on.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }
}