package horizon.http;

import horizon.core.HorizonContext;
import horizon.core.Rendezvous;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpFoyerTest {

    private HttpFoyer foyer;
    private AutoCloseable mocks;

    @Mock
    private Rendezvous<FullHttpRequest, FullHttpResponse> rendezvous;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        foyer = new HttpFoyer(0); // Use port 0 for testing to avoid binding to a real port
    }

    @AfterEach
    void tearDown() throws Exception {
        if (foyer.isOpen()) {
            foyer.close();
        }
        mocks.close();
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
    void shouldConnectToRendezvous() throws Exception {
        // When
        foyer.connectToRendezvous(rendezvous);

        // Then - verify that the rendezvous was set correctly using reflection
        Field rendezvousField = HttpFoyer.class.getDeclaredField("rendezvous");
        rendezvousField.setAccessible(true);
        Rendezvous<FullHttpRequest, FullHttpResponse> actualRendezvous = 
            (Rendezvous<FullHttpRequest, FullHttpResponse>) rendezvousField.get(foyer);

        assertThat(actualRendezvous).isSameAs(rendezvous);
    }

    @Test
    void shouldHandleHttpRequest() throws Exception {
        // Given
        foyer.connectToRendezvous(rendezvous);

        // Create a test request
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, 
            HttpMethod.GET, 
            "/test"
        );

        // Create a test response
        ByteBuf content = Unpooled.copiedBuffer("{\"result\":\"success\"}", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            content
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

        // Mock the rendezvous behavior
        HorizonContext context = mock(HorizonContext.class);
        when(rendezvous.encounter(any(FullHttpRequest.class))).thenReturn(context);
        when(rendezvous.fallAway(context)).thenReturn(response);

        // Create an embedded channel and add the HttpRequestHandler
        Object handler = createHttpRequestHandler(foyer);
        EmbeddedChannel channel = new EmbeddedChannel((io.netty.channel.ChannelHandler) handler);

        // When - write the request to the channel
        channel.writeInbound(request);

        // Then - verify that the rendezvous was called with the request
        ArgumentCaptor<FullHttpRequest> requestCaptor = ArgumentCaptor.forClass(FullHttpRequest.class);
        verify(rendezvous).encounter(requestCaptor.capture());

        // Verify the captured request
        FullHttpRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.method()).isEqualTo(HttpMethod.GET);
        assertThat(capturedRequest.uri()).isEqualTo("/test");

        // Verify that the response was written to the channel
        FullHttpResponse outboundResponse = channel.readOutbound();
        assertThat(outboundResponse).isNotNull();
        assertThat(outboundResponse.status()).isEqualTo(HttpResponseStatus.OK);
        assertThat(outboundResponse.headers().get(HttpHeaderNames.CONTENT_TYPE)).isEqualTo("application/json");

        String responseContent = outboundResponse.content().toString(CharsetUtil.UTF_8);
        assertThat(responseContent).isEqualTo("{\"result\":\"success\"}");

        // Cleanup
        channel.finish();
    }

    @Test
    void shouldHandleErrorWhenNoRendezvous() throws Exception {
        // Given - foyer without a connected rendezvous

        // Create a test request
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, 
            HttpMethod.GET, 
            "/test"
        );

        // Create an embedded channel and add the HttpRequestHandler
        Object handler = createHttpRequestHandler(foyer);
        EmbeddedChannel channel = new EmbeddedChannel((io.netty.channel.ChannelHandler) handler);

        // When - write the request to the channel
        channel.writeInbound(request);

        // Then - verify that an error response was sent
        FullHttpResponse outboundResponse = channel.readOutbound();
        assertThat(outboundResponse).isNotNull();
        assertThat(outboundResponse.status()).isEqualTo(HttpResponseStatus.SERVICE_UNAVAILABLE);

        // Cleanup
        channel.finish();
    }

    @Test
    void shouldHandleExceptionFromRendezvous() throws Exception {
        // Given
        foyer.connectToRendezvous(rendezvous);

        // Create a test request
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, 
            HttpMethod.GET, 
            "/test"
        );

        // Mock the rendezvous to throw an exception
        when(rendezvous.encounter(any(FullHttpRequest.class))).thenThrow(new RuntimeException("Test exception"));

        // Create an embedded channel and add the HttpRequestHandler
        Object handler = createHttpRequestHandler(foyer);
        EmbeddedChannel channel = new EmbeddedChannel((io.netty.channel.ChannelHandler) handler);

        // When - write the request to the channel
        channel.writeInbound(request);

        // Then - verify that an error response was sent
        FullHttpResponse outboundResponse = channel.readOutbound();
        assertThat(outboundResponse).isNotNull();
        assertThat(outboundResponse.status()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        // Cleanup
        channel.finish();
    }

    // Helper method to create an instance of the HttpRequestHandler inner class
    private Object createHttpRequestHandler(HttpFoyer foyer) throws Exception {
        Class<?> handlerClass = Class.forName("horizon.http.HttpFoyer$HttpRequestHandler");
        java.lang.reflect.Constructor<?> constructor = handlerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(foyer);
    }
}
