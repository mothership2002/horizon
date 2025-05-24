package horizon.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.Foyer;
import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket Foyer - the entry point for WebSocket connections into the Horizon framework.
 */
public class WebSocketFoyer implements Foyer<WebSocketMessage> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFoyer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final int port;
    private final AtomicBoolean isOpen = new AtomicBoolean(false);
    private final Map<String, Channel> sessions = new ConcurrentHashMap<>();
    private Rendezvous<WebSocketMessage, WebSocketMessage> rendezvous;
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public WebSocketFoyer(int port) {
        this.port = port;
    }
    
    @Override
    public void open() {
        if (isOpen.compareAndSet(false, true)) {
            logger.info("Opening WebSocket Foyer on port {}", port);
            
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            
            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast(new WebSocketFrameHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
                
                serverChannel = bootstrap.bind(port).sync().channel();
                logger.info("WebSocket Foyer opened successfully on port {}", port);
                
            } catch (Exception e) {
                logger.error("Failed to open WebSocket Foyer", e);
                close();
                throw new RuntimeException("Failed to open WebSocket Foyer", e);
            }
        }
    }
    
    @Override
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            logger.info("Closing WebSocket Foyer");
            
            // Close all sessions
            sessions.values().forEach(channel -> {
                if (channel.isActive()) {
                    channel.close();
                }
            });
            sessions.clear();
            
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
            
            logger.info("WebSocket Foyer closed");
        }
    }
    
    @Override
    public boolean isOpen() {
        return isOpen.get();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void connectToRendezvous(Rendezvous<WebSocketMessage, ?> rendezvous) {
        this.rendezvous = (Rendezvous<WebSocketMessage, WebSocketMessage>) rendezvous;
    }
    
    /**
     * Handles WebSocket frames and passes them to the Rendezvous.
     */
    private class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            String sessionId = ctx.channel().id().asShortText();
            sessions.put(sessionId, ctx.channel());
            logger.info("WebSocket client connected: {}", sessionId);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            String sessionId = ctx.channel().id().asShortText();
            sessions.remove(sessionId);
            logger.info("WebSocket client disconnected: {}", sessionId);
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
            if (frame instanceof TextWebSocketFrame) {
                handleTextFrame(ctx, (TextWebSocketFrame) frame);
            } else if (frame instanceof CloseWebSocketFrame) {
                ctx.channel().close();
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            }
        }
        
        @SuppressWarnings("unchecked")
        private void handleTextFrame(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
            String sessionId = ctx.channel().id().asShortText();
            String json = frame.text();
            
            logger.debug("Received WebSocket message from {}: {}", sessionId, json);
            
            if (rendezvous == null) {
                logger.error("No rendezvous connected");
                sendError(ctx, "Service unavailable");
                return;
            }
            
            try {
                // Parse JSON to WebSocketMessage
                Map<String, Object> messageData = objectMapper.readValue(json, Map.class);
                
                WebSocketMessage message = new WebSocketMessage();
                message.setIntent((String) messageData.get("intent"));
                message.setData((Map<String, Object>) messageData.get("data"));
                message.setSessionId(sessionId);
                
                // Encounter at the rendezvous
                HorizonContext context = rendezvous.encounter(message);
                
                // Fall away with response
                WebSocketMessage response = rendezvous.fallAway(context);
                
                // Send response
                String responseJson = objectMapper.writeValueAsString(response);
                ctx.writeAndFlush(new TextWebSocketFrame(responseJson));
                
            } catch (Exception e) {
                logger.error("Error processing WebSocket message", e);
                sendError(ctx, e.getMessage());
            }
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Unexpected error in WebSocket handler", cause);
            ctx.close();
        }
        
        private void sendError(ChannelHandlerContext ctx, String errorMessage) {
            try {
                Map<String, Object> error = new ConcurrentHashMap<>();
                error.put("error", errorMessage);
                error.put("success", false);
                
                String json = objectMapper.writeValueAsString(error);
                ctx.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                logger.error("Failed to send error message", e);
            }
        }
    }
    
    /**
     * Sends a message to a specific session.
     */
    public void sendToSession(String sessionId, WebSocketMessage message) {
        Channel channel = sessions.get(sessionId);
        if (channel != null && channel.isActive()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", sessionId, e);
            }
        }
    }
    
    /**
     * Broadcasts a message to all connected sessions.
     */
    public void broadcast(WebSocketMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            TextWebSocketFrame frame = new TextWebSocketFrame(json);
            
            sessions.values().forEach(channel -> {
                if (channel.isActive()) {
                    channel.writeAndFlush(frame.retainedDuplicate());
                }
            });
            
            frame.release();
        } catch (Exception e) {
            logger.error("Failed to broadcast message", e);
        }
    }
}
