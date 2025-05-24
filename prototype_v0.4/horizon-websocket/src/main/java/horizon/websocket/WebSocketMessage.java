package horizon.websocket;

import java.util.Map;

/**
 * Represents a WebSocket message in the Horizon framework.
 */
public class WebSocketMessage {
    private String intent;
    private Map<String, Object> data;
    private String sessionId;
    
    public WebSocketMessage() {
    }
    
    public WebSocketMessage(String intent, Map<String, Object> data) {
        this.intent = intent;
        this.data = data;
    }
    
    // Getters and setters
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
