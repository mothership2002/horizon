package horizon.demo.conductors;

import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.annotation.WebSocketResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat conductor for real-time communication.
 * Only WebSocket mappings = only WebSocket access.
 */
@Conductor(namespace = "chat")
public class ChatConductor {
    private static final Logger logger = LoggerFactory.getLogger(ChatConductor.class);
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();
    
    @Intent("join")
    @WebSocketResource(value = "chat.join", streaming = true)
    public Map<String, Object> joinChat(Map<String, Object> payload) {
        String sessionId = (String) payload.get("_sessionId");
        String username = (String) payload.get("username");
        
        activeUsers.put(sessionId, username);
        logger.info("User {} joined chat", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to chat!");
        response.put("activeUsers", activeUsers.size());
        
        return response;
    }
    
    @Intent("message")
    @WebSocketResource(value = "chat.message", streaming = true)
    public Map<String, Object> sendMessage(Map<String, Object> payload) {
        String sessionId = (String) payload.get("_sessionId");
        String message = (String) payload.get("message");
        String username = activeUsers.get(sessionId);
        
        if (username == null) {
            throw new IllegalStateException("User not joined");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("from", username);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        
        // In real implementation, would broadcast to all connected users
        return response;
    }
    
    @Intent("leave")
    @WebSocketResource("chat.leave")
    public Map<String, Object> leaveChat(Map<String, Object> payload) {
        String sessionId = (String) payload.get("_sessionId");
        String username = activeUsers.remove(sessionId);
        
        logger.info("User {} left chat", username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Goodbye!");
        
        return response;
    }
}
