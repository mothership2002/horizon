package horizon.web.websocket;

import java.util.Map;

/**
 * Represents a WebSocket message in the Horizon framework.
 * This class encapsulates the intent, data, and session ID of a WebSocket message.
 */
public class WebSocketMessage {
    private String intent;
    private Map<String, Object> data;
    private String sessionId;
    
    /**
     * Creates an empty WebSocket message.
     */
    public WebSocketMessage() {
    }
    
    /**
     * Creates a WebSocket message with the specified intent and data.
     *
     * @param intent the intent of the message
     * @param data the data of the message
     */
    public WebSocketMessage(String intent, Map<String, Object> data) {
        this.intent = intent;
        this.data = data;
    }
    
    /**
     * Gets the intent of the message.
     *
     * @return the intent
     */
    public String getIntent() {
        return intent;
    }
    
    /**
     * Sets the intent of the message.
     *
     * @param intent the intent to set
     */
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    /**
     * Gets the data of the message.
     *
     * @return the data
     */
    public Map<String, Object> getData() {
        return data;
    }
    
    /**
     * Sets the data of the message.
     *
     * @param data the data to set
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    /**
     * Gets the session ID of the message.
     *
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Sets the session ID of the message.
     *
     * @param sessionId the session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}