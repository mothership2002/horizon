package horizon.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class HttpProtocolAdapterTest {

    private HttpProtocolAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new HttpProtocolAdapter();
    }
    
    @Test
    void shouldExtractIntentFromSimplePath() {
        // Given
        FullHttpRequest request = createRequest("/users/create", HttpMethod.POST);
        
        // When
        String intent = adapter.extractIntent(request);
        
        // Then
        assertThat(intent).isEqualTo("user.create");
    }
    
    @Test
    void shouldExtractIntentFromPathWithId() {
        // Given
        FullHttpRequest request = createRequest("/users/1234", HttpMethod.GET);
        
        // When
        String intent = adapter.extractIntent(request);
        
        // Then
        assertThat(intent).isEqualTo("user");
    }
    
    @Test
    void shouldExtractIntentFromRootPath() {
        // Given
        FullHttpRequest request = createRequest("/", HttpMethod.GET);
        
        // When
        String intent = adapter.extractIntent(request);
        
        // Then
        assertThat(intent).isEqualTo("welcome");
    }
    
    @Test
    void shouldExtractIntentFromApiPath() {
        // Given
        FullHttpRequest request = createRequest("/api/users/create", HttpMethod.POST);
        
        // When
        String intent = adapter.extractIntent(request);
        
        // Then
        assertThat(intent).isEqualTo("user.create");
    }
    
    @Test
    void shouldExtractPayloadFromQueryParams() {
        // Given
        FullHttpRequest request = createRequest("/users?name=John&email=john@example.com", HttpMethod.GET);
        
        // When
        Object payload = adapter.extractPayload(request);
        
        // Then
        assertThat(payload).isInstanceOf(Map.class);
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        assertThat(payloadMap)
            .containsEntry("name", "John")
            .containsEntry("email", "john@example.com")
            .containsEntry("_method", "GET");
    }
    
    @Test
    void shouldExtractPayloadFromJsonBody() {
        // Given
        String json = "{\"name\":\"John\",\"email\":\"john@example.com\"}";
        FullHttpRequest request = createRequestWithJsonBody("/users/create", HttpMethod.POST, json);
        
        // When
        Object payload = adapter.extractPayload(request);
        
        // Then
        assertThat(payload).isInstanceOf(Map.class);
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        assertThat(payloadMap)
            .containsEntry("name", "John")
            .containsEntry("email", "john@example.com")
            .containsEntry("_method", "POST");
    }
    
    @Test
    void shouldExtractIdFromPath() {
        // Given
        FullHttpRequest request = createRequest("/users/1234", HttpMethod.GET);
        
        // When
        Object payload = adapter.extractPayload(request);
        
        // Then
        assertThat(payload).isInstanceOf(Map.class);
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        assertThat(payloadMap)
            .containsEntry("id", 1234L)
            .containsEntry("_method", "GET");
    }
    
    @Test
    void shouldBuildSuccessResponse() {
        // Given
        FullHttpRequest request = createRequest("/users", HttpMethod.GET);
        Map<String, Object> result = new HashMap<>();
        result.put("id", 1234L);
        result.put("name", "John");
        
        // When
        FullHttpResponse response = adapter.buildResponse(result, request);
        
        // Then
        assertThat(response.status()).isEqualTo(HttpResponseStatus.OK);
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).contains("application/json");
        
        String content = response.content().toString(CharsetUtil.UTF_8);
        assertThat(content).contains("\"id\":1234");
        assertThat(content).contains("\"name\":\"John\"");
    }
    
    @Test
    void shouldBuildErrorResponse() {
        // Given
        FullHttpRequest request = createRequest("/users", HttpMethod.GET);
        IllegalArgumentException error = new IllegalArgumentException("Invalid user ID");
        
        // When
        FullHttpResponse response = adapter.buildErrorResponse(error, request);
        
        // Then
        assertThat(response.status()).isEqualTo(HttpResponseStatus.BAD_REQUEST);
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).contains("application/json");
        
        String content = response.content().toString(CharsetUtil.UTF_8);
        assertThat(content).contains("\"error\":\"Invalid user ID\"");
        assertThat(content).contains("\"type\":\"IllegalArgumentException\"");
    }
    
    @Test
    void shouldBuildServerErrorResponse() {
        // Given
        FullHttpRequest request = createRequest("/users", HttpMethod.GET);
        RuntimeException error = new RuntimeException("Internal error");
        
        // When
        FullHttpResponse response = adapter.buildErrorResponse(error, request);
        
        // Then
        assertThat(response.status()).isEqualTo(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).contains("application/json");
        
        String content = response.content().toString(CharsetUtil.UTF_8);
        assertThat(content).contains("\"error\":\"Internal error\"");
        assertThat(content).contains("\"type\":\"RuntimeException\"");
    }
    
    // Helper methods
    private FullHttpRequest createRequest(String uri, HttpMethod method) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri);
    }
    
    private FullHttpRequest createRequestWithJsonBody(String uri, HttpMethod method, String json) {
        ByteBuf content = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, content);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return request;
    }
}