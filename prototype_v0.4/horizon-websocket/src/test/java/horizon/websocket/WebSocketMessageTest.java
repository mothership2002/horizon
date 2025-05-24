package horizon.websocket;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketMessageTest {

    @Test
    void shouldCreateEmptyMessage() {
        // When
        WebSocketMessage message = new WebSocketMessage();
        
        // Then
        assertThat(message.getIntent()).isNull();
        assertThat(message.getData()).isNull();
        assertThat(message.getSessionId()).isNull();
    }
    
    @Test
    void shouldCreateMessageWithIntentAndData() {
        // Given
        String intent = "user.create";
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("email", "john@example.com");
        
        // When
        WebSocketMessage message = new WebSocketMessage(intent, data);
        
        // Then
        assertThat(message.getIntent()).isEqualTo(intent);
        assertThat(message.getData()).isEqualTo(data);
        assertThat(message.getSessionId()).isNull();
    }
    
    @Test
    void shouldSetAndGetIntent() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        String intent = "user.create";
        
        // When
        message.setIntent(intent);
        
        // Then
        assertThat(message.getIntent()).isEqualTo(intent);
    }
    
    @Test
    void shouldSetAndGetData() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("email", "john@example.com");
        
        // When
        message.setData(data);
        
        // Then
        assertThat(message.getData()).isEqualTo(data);
    }
    
    @Test
    void shouldSetAndGetSessionId() {
        // Given
        WebSocketMessage message = new WebSocketMessage();
        String sessionId = "session123";
        
        // When
        message.setSessionId(sessionId);
        
        // Then
        assertThat(message.getSessionId()).isEqualTo(sessionId);
    }
}