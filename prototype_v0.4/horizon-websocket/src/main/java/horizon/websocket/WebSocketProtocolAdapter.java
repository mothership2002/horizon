package horizon.websocket;

import horizon.core.protocol.ProtocolAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts WebSocket messages to Horizon format.
 */
public class WebSocketProtocolAdapter implements ProtocolAdapter<WebSocketMessage, WebSocketMessage> {
    
    @Override
    public String extractIntent(WebSocketMessage message) {
        return message.getIntent();
    }
    
    @Override
    public Object extractPayload(WebSocketMessage message) {
        Map<String, Object> payload = new HashMap<>();
        if (message.getData() != null) {
            payload.putAll(message.getData());
        }
        payload.put("_sessionId", message.getSessionId());
        return payload;
    }
    
    @Override
    public WebSocketMessage buildResponse(Object result, WebSocketMessage request) {
        WebSocketMessage response = new WebSocketMessage();
        response.setIntent(request.getIntent() + ".response");
        
        Map<String, Object> data = new HashMap<>();
        data.put("result", result);
        data.put("success", true);
        response.setData(data);
        
        response.setSessionId(request.getSessionId());
        return response;
    }
    
    @Override
    public WebSocketMessage buildErrorResponse(Throwable error, WebSocketMessage request) {
        WebSocketMessage response = new WebSocketMessage();
        response.setIntent(request.getIntent() + ".error");
        
        Map<String, Object> data = new HashMap<>();
        data.put("error", error.getMessage());
        data.put("type", error.getClass().getSimpleName());
        data.put("success", false);
        response.setData(data);
        
        response.setSessionId(request.getSessionId());
        return response;
    }
}
