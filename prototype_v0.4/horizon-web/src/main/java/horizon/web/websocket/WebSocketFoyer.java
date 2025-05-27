package horizon.web.websocket;

import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import horizon.core.util.JsonUtils;
import horizon.web.common.AbstractWebFoyer;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Foyer - the entry point for WebSocket connections into the Horizon framework.
 * This class extends AbstractWebFoyer to provide WebSocket-specific functionality.
 * Uses JsonUtils for JSON operations to ensure consistent ObjectMapper usage across the application.
 */
public class WebSocketFoyer extends AbstractWebFoyer<WebSocketMessage> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFoyer.class);

    private final Map<String, Channel> sessions = new ConcurrentHashMap<>();

    public WebSocketFoyer(int port) {
        super(port);
    }

    @Override
    protected String getProtocolName() {
        return "WebSocket";
    }

    @Override
    protected ChannelInitializer<?> createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(65536));
                pipeline.addLast(new ChunkedWriteHandler());
                pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                pipeline.addLast(new WebSocketFrameHandler());
            }
        };
    }

    @Override
    public void close() {
        // Close all sessions
        sessions.values().forEach(channel -> {
            if (channel.isActive()) {
                channel.close();
            }
        });
        sessions.clear();

        // Call parent close method
        super.close();
    }

    /**
     * Sends a message to a specific session.
     *
     * @param sessionId the session ID
     * @param message the message to send
     */
    public void sendToSession(String sessionId, WebSocketMessage message) {
        Channel channel = sessions.get(sessionId);
        if (channel != null && channel.isActive()) {
            try {
                String json = JsonUtils.toJson(message);
                channel.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", sessionId, e);
            }
        }
    }

    /**
     * Broadcasts a message to all connected sessions.
     *
     * @param message the message to broadcast
     */
    public void broadcast(WebSocketMessage message) {
        try {
            String json = JsonUtils.toJson(message);
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
                Map<String, Object> messageData = JsonUtils.fromJson(json, Map.class);

                WebSocketMessage message = new WebSocketMessage();
                message.setIntent((String) messageData.get("intent"));
                message.setData((Map<String, Object>) messageData.get("data"));
                message.setSessionId(sessionId);

                // Encounter at the rendezvous
                HorizonContext context = rendezvous.encounter(message);

                // Fall away with response
                WebSocketMessage response = (WebSocketMessage) rendezvous.fallAway(context);

                // Send response
                String responseJson = JsonUtils.toJson(response);
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

                String json = JsonUtils.toJson(error);
                ctx.writeAndFlush(new TextWebSocketFrame(json));
            } catch (Exception e) {
                logger.error("Failed to send error message", e);
            }
        }
    }
}
