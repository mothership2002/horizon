package horizon.websocket;

import horizon.core.protocol.ProtocolAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketProtocolTest {

    @Test
    void shouldReturnCorrectName() {
        // Given
        WebSocketProtocol protocol = new WebSocketProtocol();
        
        // When
        String name = protocol.getName();
        
        // Then
        assertThat(name).isEqualTo("WebSocket");
    }
    
    @Test
    void shouldCreateWebSocketProtocolAdapter() {
        // Given
        WebSocketProtocol protocol = new WebSocketProtocol();
        
        // When
        ProtocolAdapter<WebSocketMessage, WebSocketMessage> adapter = protocol.createAdapter();
        
        // Then
        assertThat(adapter).isNotNull();
        assertThat(adapter).isInstanceOf(WebSocketProtocolAdapter.class);
    }
}