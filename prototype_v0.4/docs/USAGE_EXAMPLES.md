# Horizon Framework Usage Examples

This document provides examples of how to use the Horizon Framework in your applications.

## Basic Setup

Here's a basic example of setting up the Horizon Framework in your application:

```java
public class Application {
    public static void main(String[] args) {
        // Create the Protocol Aggregator - the heart of Horizon
        ProtocolAggregator aggregator = new ProtocolAggregator();
        
        // Register protocols
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        
        // Scan and register conductors
        aggregator.scanConductors("com.example.conductors");
        
        // Start the aggregator
        aggregator.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            aggregator.stop();
        }));
    }
}
```

## Creating Conductors

### Interface-Based Conductor

You can create a conductor by implementing the `Conductor` interface:

```java
public class UserConductor implements Conductor<Map<String, Object>, Map<String, Object>> {
    
    @Override
    public Map<String, Object> conduct(Map<String, Object> payload) {
        // Extract data from payload
        String name = (String) payload.get("name");
        String email = (String) payload.get("email");
        
        // Validate
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        // Process
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1001L);
        user.put("name", name);
        user.put("email", email);
        user.put("createdAt", System.currentTimeMillis());
        
        return user;
    }
    
    @Override
    public String getIntentPattern() {
        return "user.create";
    }
}
```

### Annotation-Based Conductor

A more declarative approach is to use annotations:

```java
@Conductor(namespace = "user")
public class UserConductor {
    
    @Intent("create")
    @HttpResource("POST /users")
    @WebSocketResource("user.create")
    public User createUser(CreateUserRequest request) {
        // Extract and validate
        String name = request.getName();
        String email = request.getEmail();
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        
        // Create user
        User user = new User();
        user.setId(1001L);
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(System.currentTimeMillis());
        
        return user;
    }
    
    @Intent("get")
    @HttpResource("GET /users/{id}")
    @WebSocketResource("user.get")
    public User getUser(GetUserRequest request) {
        Long id = request.getId();
        
        // Fetch user by ID
        User user = new User();
        user.setId(id);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        
        return user;
    }
}
```

## Protocol Access Control

You can control which protocols can access your conductors or intent methods:

```java
@Conductor(namespace = "admin")
@ProtocolAccess({"HTTP"})  // Admin operations only via HTTP
public class AdminConductor {
    
    @Intent("shutdown")
    @HttpResource("POST /admin/shutdown")
    public Map<String, Object> shutdown() {
        // Shutdown logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shutdown initiated");
        return response;
    }
}

@Conductor(namespace = "chat")
@ProtocolAccess({"WebSocket"})  // Chat operations only via WebSocket
public class ChatConductor {
    
    @Intent("join")
    @WebSocketResource("chat.join")
    public Map<String, Object> joinChat(Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        String userId = (String) payload.get("userId");
        
        // Join chat room logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Joined chat room: " + roomId);
        return response;
    }
}
```

## Method-Level Protocol Access

You can also control protocol access at the method level:

```java
@Conductor(namespace = "user")
public class UserConductor {
    
    @Intent("create")
    @HttpResource("POST /users")
    @WebSocketResource("user.create")
    public User createUser(CreateUserRequest request) {
        // Accessible via both HTTP and WebSocket
    }
    
    @Intent("bulkCreate")
    @HttpResource("POST /users/bulk-create")
    @ProtocolAccess({"HTTP"})  // Only HTTP can access
    public BulkCreateUserResponse bulkCreateUsers(BulkCreateUserRequest request) {
        // Accessible only via HTTP
    }
}
```

## Intent Aliases

You can define aliases for your intents:

```java
@Conductor(namespace = "user")
public class UserConductor {
    
    @Intent(value = "create", aliases = {"add", "register"})
    @HttpResource("POST /users")
    @WebSocketResource("user.create")
    public User createUser(CreateUserRequest request) {
        // Can be accessed via "user.create", "user.add", or "user.register"
    }
}
```

## HTTP Protocol Examples

### HTTP Request Handling

When using the HTTP protocol, requests are mapped to intents based on the HTTP method and path:

```java
@Conductor(namespace = "user")
public class UserConductor {
    
    @Intent("create")
    @HttpResource("POST /users")
    public User createUser(CreateUserRequest request) {
        // Handles POST /users
    }
    
    @Intent("get")
    @HttpResource("GET /users/{id}")
    public User getUser(GetUserRequest request) {
        // Handles GET /users/123
        // The ID is extracted from the path and added to the payload
    }
    
    @Intent("update")
    @HttpResource("PUT /users/{id}")
    public User updateUser(UpdateUserRequest request) {
        // Handles PUT /users/123
    }
    
    @Intent("delete")
    @HttpResource("DELETE /users/{id}")
    public DeleteUserResponse deleteUser(DeleteUserRequest request) {
        // Handles DELETE /users/123
    }
    
    @Intent("list")
    @HttpResource("GET /users")
    public UserListResponse listUsers() {
        // Handles GET /users
    }
    
    @Intent("search")
    @HttpResource("GET /users/search")
    public SearchUserResponse searchUsers(SearchUserRequest request) {
        // Handles GET /users/search?q=john&searchBy=name
        // Query parameters are added to the payload
    }
}
```

## WebSocket Protocol Examples

### WebSocket Message Handling

When using the WebSocket protocol, messages are mapped to intents based on the intent field in the message:

```java
@Conductor(namespace = "chat")
@ProtocolAccess({"WebSocket"})
public class ChatConductor {
    
    @Intent("join")
    @WebSocketResource("chat.join")
    public Map<String, Object> joinChat(Map<String, Object> payload) {
        // Handles WebSocket message with intent "chat.join"
        String roomId = (String) payload.get("roomId");
        String userId = (String) payload.get("userId");
        
        // Join chat room logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Joined chat room: " + roomId);
        return response;
    }
    
    @Intent("message")
    @WebSocketResource("chat.message")
    public Map<String, Object> sendMessage(Map<String, Object> payload) {
        // Handles WebSocket message with intent "chat.message"
        String roomId = (String) payload.get("roomId");
        String userId = (String) payload.get("userId");
        String message = (String) payload.get("message");
        
        // Send message logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message sent to room: " + roomId);
        return response;
    }
    
    @Intent("leave")
    @WebSocketResource("chat.leave")
    public Map<String, Object> leaveChat(Map<String, Object> payload) {
        // Handles WebSocket message with intent "chat.leave"
        String roomId = (String) payload.get("roomId");
        String userId = (String) payload.get("userId");
        
        // Leave chat room logic
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Left chat room: " + roomId);
        return response;
    }
}
```

### WebSocket Client Example

Here's an example of how to connect to the WebSocket server and send messages:

```javascript
// Connect to WebSocket server
const socket = new WebSocket('ws://localhost:8081/ws');

// Send a message
socket.onopen = function() {
    const message = {
        intent: 'chat.join',
        data: {
            roomId: 'room1',
            userId: 'user123'
        }
    };
    socket.send(JSON.stringify(message));
};

// Handle response
socket.onmessage = function(event) {
    const response = JSON.parse(event.data);
    console.log('Received response:', response);
};
```

## Conclusion

These examples demonstrate how to use the Horizon Framework to handle multiple protocols with a single business logic implementation. The framework provides a clean, declarative, and powerful way to build applications that need to speak multiple protocols.