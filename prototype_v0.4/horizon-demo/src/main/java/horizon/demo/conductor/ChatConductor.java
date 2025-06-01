package horizon.demo.conductor;

import horizon.core.annotation.*;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Chat conductor for real-time messaging.
 * Primarily designed for WebSocket but also accessible via HTTP for certain operations.
 * 
 * This conductor demonstrates how to handle real-time, session-based interactions
 * in the Horizon Framework.
 */
@Conductor(namespace = "chat")
@ProtocolAccess({ProtocolNames.WEBSOCKET, ProtocolNames.HTTP})
public class ChatConductor {
    private static final Logger logger = LoggerFactory.getLogger(ChatConductor.class);
    
    // Active sessions and messages
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final List<ChatMessage> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private final int MAX_HISTORY = 100;
    
    /**
     * Join the chat room.
     * Protocol mappings:
     * - WebSocket: chat.join
     * - HTTP: POST /chat/join (for testing)
     */
    @Intent("join")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "WebSocket", value = "chat.join"),
            @ProtocolSchema(protocol = "HTTP", value = "POST /chat/join")
        }
    )
    public Map<String, Object> joinChat(
        @Param("username") String username,
        @Param(value = "_sessionId", required = false) String sessionId
    ) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        // For HTTP, generate a session ID
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        
        logger.info("User {} joining chat with session {}", username, sessionId);
        
        // Check if username is already taken
        boolean usernameTaken = sessions.values().stream()
            .anyMatch(s -> s.username.equalsIgnoreCase(username));
        
        if (usernameTaken) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Username already taken");
            return response;
        }
        
        // Create session
        ChatSession session = new ChatSession(sessionId, username, Instant.now());
        sessions.put(sessionId, session);
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Welcome to the chat, " + username + "!");
        response.put("sessionId", sessionId);
        response.put("activeUsers", getActiveUsers());
        response.put("recentMessages", getRecentMessages(10));
        
        // Broadcast user joined (in real app, would use WebSocket broadcast)
        broadcastSystemMessage(username + " joined the chat");
        
        return response;
    }
    
    /**
     * Send a chat message.
     * Protocol mappings:
     * - WebSocket: chat.message
     * - HTTP: POST /chat/message (for testing)
     */
    @Intent("message")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "WebSocket", value = "chat.message"),
            @ProtocolSchema(protocol = "HTTP", value = "POST /chat/message")
        }
    )
    public Map<String, Object> sendMessage(
        @Param("message") String message,
        @Param(value = "_sessionId", required = false) String sessionId,
        @Param(value = "sessionId", required = false) String httpSessionId
    ) {
        // Support both WebSocket automatic session ID and HTTP explicit session ID
        String sid = sessionId != null ? sessionId : httpSessionId;
        
        if (sid == null) {
            throw new IllegalArgumentException("Session ID required");
        }
        
        ChatSession session = sessions.get(sid);
        if (session == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not joined to chat");
            return response;
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        
        logger.info("Message from {}: {}", session.username, message);
        
        // Create chat message
        ChatMessage chatMessage = new ChatMessage(
            UUID.randomUUID().toString(),
            session.username,
            message,
            Instant.now()
        );
        
        // Add to history
        messageHistory.add(chatMessage);
        if (messageHistory.size() > MAX_HISTORY) {
            messageHistory.remove(0);
        }
        
        // Update session activity
        session.lastActivity = Instant.now();
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messageId", chatMessage.id);
        response.put("timestamp", chatMessage.timestamp.toString());
        
        // In a real implementation, would broadcast to all connected users
        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("type", "message");
        broadcast.put("from", chatMessage.username);
        broadcast.put("message", chatMessage.message);
        broadcast.put("timestamp", chatMessage.timestamp.toString());
        response.put("broadcast", broadcast);
        
        return response;
    }
    
    /**
     * Leave the chat room.
     * Protocol mappings:
     * - WebSocket: chat.leave
     * - HTTP: POST /chat/leave
     */
    @Intent("leave")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "WebSocket", value = "chat.leave"),
            @ProtocolSchema(protocol = "HTTP", value = "POST /chat/leave")
        }
    )
    public Map<String, Object> leaveChat(
        @Param(value = "_sessionId", required = false) String sessionId,
        @Param(value = "sessionId", required = false) String httpSessionId
    ) {
        String sid = sessionId != null ? sessionId : httpSessionId;
        
        if (sid == null) {
            throw new IllegalArgumentException("Session ID required");
        }
        
        ChatSession session = sessions.remove(sid);
        
        Map<String, Object> response = new HashMap<>();
        if (session != null) {
            logger.info("User {} left chat", session.username);
            
            response.put("success", true);
            response.put("message", "Goodbye, " + session.username + "!");
            
            // Broadcast user left
            broadcastSystemMessage(session.username + " left the chat");
        } else {
            response.put("success", false);
            response.put("message", "Not in chat");
        }
        
        return response;
    }
    
    /**
     * Get active users in the chat.
     * Protocol mappings:
     * - WebSocket: chat.users
     * - HTTP: GET /chat/users
     */
    @Intent("users")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "WebSocket", value = "chat.users"),
            @ProtocolSchema(protocol = "HTTP", value = "GET /chat/users")
        }
    )
    public Map<String, Object> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", getActiveUsers());
        response.put("count", sessions.size());
        response.put("timestamp", Instant.now().toString());
        
        return response;
    }
    
    /**
     * Get chat history.
     * Protocol mappings:
     * - WebSocket: chat.history
     * - HTTP: GET /chat/history
     */
    @Intent("history")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "WebSocket", value = "chat.history"),
            @ProtocolSchema(protocol = "HTTP", value = "GET /chat/history")
        }
    )
    public Map<String, Object> getHistory(
        @Param(value = "limit", defaultValue = "20") int limit,
        @Param(value = "before", required = false) String beforeId
    ) {
        List<Map<String, Object>> messages;
        
        if (beforeId != null) {
            // Get messages before a specific message ID
            int index = -1;
            for (int i = 0; i < messageHistory.size(); i++) {
                if (messageHistory.get(i).id.equals(beforeId)) {
                    index = i;
                    break;
                }
            }
            
            if (index > 0) {
                int start = Math.max(0, index - limit);
                messages = messageHistory.subList(start, index).stream()
                    .map(this::messageToMap)
                    .collect(Collectors.toList());
            } else {
                messages = new ArrayList<>();
            }
        } else {
            // Get most recent messages
            messages = getRecentMessages(limit);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("count", messages.size());
        response.put("hasMore", messages.size() == limit);
        
        return response;
    }
    
    /**
     * Send a typing indicator.
     * Protocol mappings:
     * - WebSocket: chat.typing
     */
    @Intent("typing")
    @ProtocolAccess(
        schema = @ProtocolSchema(protocol = "WebSocket", value = "chat.typing")
    )
    public Map<String, Object> typing(
        @Param(value = "_sessionId") String sessionId,
        @Param(value = "typing", defaultValue = "true") boolean isTyping
    ) {
        ChatSession session = sessions.get(sessionId);
        if (session == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Not in chat");
            return response;
        }
        
        // Update typing status
        session.isTyping = isTyping;
        session.lastActivity = Instant.now();
        
        // In real implementation, would broadcast typing status
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> broadcast = new HashMap<>();
        broadcast.put("type", "typing");
        broadcast.put("username", session.username);
        broadcast.put("typing", isTyping);
        response.put("broadcast", broadcast);
        
        return response;
    }
    
    // Helper methods
    
    private List<Map<String, Object>> getActiveUsers() {
        return sessions.values().stream()
            .map(session -> {
                Map<String, Object> user = new HashMap<>();
                user.put("username", session.username);
                user.put("joinedAt", session.joinedAt.toString());
                user.put("typing", session.isTyping);
                return user;
            })
            .collect(Collectors.toList());
    }
    
    private List<Map<String, Object>> getRecentMessages(int limit) {
        int start = Math.max(0, messageHistory.size() - limit);
        return messageHistory.subList(start, messageHistory.size()).stream()
            .map(this::messageToMap)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> messageToMap(ChatMessage msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", msg.id);
        map.put("from", msg.username);
        map.put("message", msg.message);
        map.put("timestamp", msg.timestamp.toString());
        map.put("type", msg.username.equals("System") ? "system" : "user");
        return map;
    }
    
    private void broadcastSystemMessage(String message) {
        ChatMessage systemMessage = new ChatMessage(
            UUID.randomUUID().toString(),
            "System",
            message,
            Instant.now()
        );
        messageHistory.add(systemMessage);
        
        if (messageHistory.size() > MAX_HISTORY) {
            messageHistory.remove(0);
        }
    }
    
    // Data classes
    
    private static class ChatSession {
        final String sessionId;
        final String username;
        final Instant joinedAt;
        Instant lastActivity;
        boolean isTyping;
        
        ChatSession(String sessionId, String username, Instant joinedAt) {
            this.sessionId = sessionId;
            this.username = username;
            this.joinedAt = joinedAt;
            this.lastActivity = joinedAt;
            this.isTyping = false;
        }
    }
    
    private static class ChatMessage {
        final String id;
        final String username;
        final String message;
        final Instant timestamp;
        
        ChatMessage(String id, String username, String message, Instant timestamp) {
            this.id = id;
            this.username = username;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}
