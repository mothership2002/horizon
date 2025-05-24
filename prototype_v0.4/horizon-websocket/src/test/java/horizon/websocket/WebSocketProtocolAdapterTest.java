package horizon.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketProtocolAdapterTest {

    private WebSocketProtocolAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new WebSocketProtocolAdapter();
    }
    
    @Test
    void shouldExtractIntent() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        message.setIntent("user.create");
        
        // When
        String intent = adapter.extractIntent(message);
        
        // Then
        assertThat(intent).isEqualTo("user.create");
    }
    
    @Test
    void shouldExtractPayload() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        message.setSessionId("session123");
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("email", "john@example.com");
        message.setData(data);
        
        // When
        Object payload = adapter.extractPayload(message);
        
        // Then
        assertThat(payload).isInstanceOf(Map.class);
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        assertThat(payloadMap)
            .containsEntry("name", "John")
            .containsEntry("email", "john@example.com")
            .containsEntry("_sessionId", "session123");
    }
    
    @Test
    void shouldExtractPayloadWithNullData() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        message.setSessionId("session123");
        message.setData(null);
        
        // When
        Object payload = adapter.extractPayload(message);
        
        // Then
        assertThat(payload).isInstanceOf(Map.class);
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        assertThat(payloadMap)
            .containsEntry("_sessionId", "session123")
            .hasSize(1);
    }
    
    @Test
    void shouldBuildResponse() {
        // Given
        WebSocketMessage request = new WebSocketMessage();
        request.setIntent("user.create");
        request.setSessionId("session123");
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", 1234L);
        result.put("name", "John");
        
        // When
        WebSocketMessage response = adapter.buildResponse(result, request);
        
        // Then
        assertThat(response.getIntent()).isEqualTo("user.create.response");
        assertThat(response.getSessionId()).isEqualTo("session123");
        assertThat(response.getData())
            .containsEntry("result", result)
            .containsEntry("success", true);
    }
    
    @Test
    void shouldBuildErrorResponse() {
        // Given
        WebSocketMessage request = new WebSocketMessage();
        request.setIntent("user.create");
        request.setSessionId("session123");
        
        IllegalArgumentException error = new IllegalArgumentException("Invalid user data");
        
        // When
        WebSocketMessage response = adapter.buildErrorResponse(error, request);
        
        // Then
        assertThat(response.getIntent()).isEqualTo("user.create.error");
        assertThat(response.getSessionId()).isEqualTo("session123");
        assertThat(response.getData())
            .containsEntry("error", "Invalid user data")
            .containsEntry("type", "IllegalArgumentException")
            .containsEntry("success", false);
    }
}