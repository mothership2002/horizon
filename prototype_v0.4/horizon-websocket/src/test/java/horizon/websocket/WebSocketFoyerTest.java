package horizon.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WebSocketFoyerTest {

    private WebSocketFoyer foyer;
    private AutoCloseable mocks;
    private ObjectMapper objectMapper = new ObjectMapper();

    private TestRendezvous rendezvous;

    @BeforeEach
    void setUp() {
        // Create a concrete implementation instead of using Mockito
        rendezvous = new TestRendezvous();
        foyer = new WebSocketFoyer(0); // Use port 0 for testing to avoid binding to a real port
    }

    // Use EmbeddedChannel for testing instead of creating our own Channel implementation
    private static class TestChannel extends EmbeddedChannel {
        private final AtomicReference<Object> lastWritten = new AtomicReference<>();

        @Override
        public ChannelFuture writeAndFlush(Object msg) {
            lastWritten.set(msg);
            return super.writeAndFlush(msg);
        }

        public Object getLastWritten() {
            return lastWritten.get();
        }
    }

    // Concrete implementation of Rendezvous for testing
    private static class TestRendezvous implements Rendezvous<WebSocketMessage, WebSocketMessage> {
        private HorizonContext lastContext;
        private WebSocketMessage lastMessage;
        private WebSocketMessage responseMessage;

        public TestRendezvous() {
            this.responseMessage = new WebSocketMessage("user.create.response", new HashMap<>());
        }

        @Override
        public HorizonContext encounter(WebSocketMessage input) {
            this.lastMessage = input;
            this.lastContext = mock(HorizonContext.class);
            return lastContext;
        }

        @Override
        public WebSocketMessage fallAway(HorizonContext context) {
            return responseMessage;
        }

        public HorizonContext getLastContext() {
            return lastContext;
        }

        public WebSocketMessage getLastMessage() {
            return lastMessage;
        }

        public void setResponseMessage(WebSocketMessage responseMessage) {
            this.responseMessage = responseMessage;
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (foyer.isOpen()) {
            foyer.close();
        }
        // No need to close mocks as we're not using MockitoAnnotations.openMocks anymore
    }

    @Test
    void shouldOpenAndClose() {
        // When
        foyer.open();

        // Then
        assertThat(foyer.isOpen()).isTrue();

        // When
        foyer.close();

        // Then
        assertThat(foyer.isOpen()).isFalse();
    }

    @Test
    void shouldConnectToRendezvous() {
        // When
        foyer.connectToRendezvous(rendezvous);

        // Then - no direct way to verify, but we'll test the handler below
    }

    @Test
    void shouldHandleWebSocketFrame() throws Exception {
        // Given
        foyer.connectToRendezvous(rendezvous);

        // Set up the response message in our TestRendezvous
        WebSocketMessage responseMessage = new WebSocketMessage("user.create.response", new HashMap<>());
        rendezvous.setResponseMessage(responseMessage);

        // Create a test channel and mock context
        TestChannel channel = new TestChannel();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        // Create a WebSocket frame with a message
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("intent", "user.create");
        messageData.put("data", new HashMap<String, Object>());

        String json = objectMapper.writeValueAsString(messageData);
        TextWebSocketFrame frame = new TextWebSocketFrame(json);

        // Get the handler through reflection (not ideal, but necessary for testing)
        Object handler = getWebSocketFrameHandler(foyer);

        // When - simulate channelActive and channelRead0
        invokeChannelActive(handler, ctx);
        invokeChannelRead0(handler, ctx, frame);

        // Then
        // Verify that the message was processed by checking the TestRendezvous
        assertThat(rendezvous.getLastMessage()).isNotNull();
        assertThat(rendezvous.getLastMessage().getIntent()).isEqualTo("user.create");

        // Verify that the response was sent
        verify(ctx).writeAndFlush(any(TextWebSocketFrame.class));
    }

    @Test
    void shouldSendToSession() throws Exception {
        // Given
        // Add a session to the foyer using reflection
        String sessionId = "session123";
        TestChannel channel = new TestChannel();

        Map<String, Channel> sessions = getSessionsMap(foyer);
        sessions.put(sessionId, channel);

        // Create a message to send
        WebSocketMessage message = new WebSocketMessage("notification", new HashMap<>());

        // When
        foyer.sendToSession(sessionId, message);

        // Then
        assertThat(channel.getLastWritten()).isInstanceOf(TextWebSocketFrame.class);
    }

    @Test
    void shouldBroadcastToAllSessions() throws Exception {
        // Given
        // Add multiple sessions to the foyer using reflection
        TestChannel channel1 = new TestChannel();
        TestChannel channel2 = new TestChannel();

        Map<String, Channel> sessions = getSessionsMap(foyer);
        sessions.put("session1", channel1);
        sessions.put("session2", channel2);

        // Create a message to broadcast
        WebSocketMessage message = new WebSocketMessage("broadcast", new HashMap<>());

        // When
        foyer.broadcast(message);

        // Then
        assertThat(channel1.getLastWritten()).isInstanceOf(TextWebSocketFrame.class);
        assertThat(channel2.getLastWritten()).isInstanceOf(TextWebSocketFrame.class);
    }

    // Helper methods for reflection access to private members
    private Object getWebSocketFrameHandler(WebSocketFoyer foyer) throws Exception {
        // This is a bit hacky, but necessary for testing private inner classes
        Class<?> handlerClass = Class.forName("horizon.websocket.WebSocketFoyer$WebSocketFrameHandler");
        Constructor<?> constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(foyer);
    }

    private void invokeChannelActive(Object handler, ChannelHandlerContext ctx) throws Exception {
        Class<?> handlerClass = handler.getClass();
        handlerClass.getDeclaredMethod("channelActive", ChannelHandlerContext.class)
            .invoke(handler, ctx);
    }

    private void invokeChannelRead0(Object handler, ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        Class<?> handlerClass = handler.getClass();
        handlerClass.getDeclaredMethod("channelRead0", ChannelHandlerContext.class, Object.class)
            .invoke(handler, ctx, frame);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Channel> getSessionsMap(WebSocketFoyer foyer) throws Exception {
        java.lang.reflect.Field sessionsField = WebSocketFoyer.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        return (Map<String, Channel>) sessionsField.get(foyer);
    }
}
