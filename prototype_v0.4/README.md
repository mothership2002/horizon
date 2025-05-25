# Horizon Framework v0.4

> A Protocol Aggregation Framework for the Multi-Protocol Era

## Philosophy

In today's world, applications need to speak multiple protocols:
- REST APIs for web clients
- WebSocket for real-time features  
- gRPC for microservices
- GraphQL for flexible queries

**Horizon** solves this by providing a unified way to handle all protocols with a single business logic implementation.

## Core Concepts

### Rendezvous
The meeting point where all protocols converge. No matter how a request arrives, it meets your business logic at the Rendezvous.

### Conductor
The orchestrator of your business logic. A Conductor interprets intents and conducts the appropriate actions.

### Foyer
The entry point for each protocol. Each protocol has its own Foyer that adapts protocol-specific requests to the common Horizon format.

## Quick Example

```java
// Your business logic - written once, with security controls
@Conductor(namespace = "user")
@ProtocolAccess({"HTTP", "WebSocket"})  // Explicit protocol access
public class UserConductor {
    
    @Intent("create")
    @ProtocolMapping(protocol = "HTTP", mapping = {"POST /users"})
    @ProtocolMapping(protocol = "WebSocket", mapping = {"user.create"})
    public User createUser(CreateUserRequest request) {
        return userService.createUser(request);
    }
    
    @Intent("bulkImport")
    @ProtocolMapping(protocol = "HTTP", mapping = {"POST /users/import"})
    @ProtocolAccess({"HTTP"})  // Only accessible via HTTP
    public ImportResult importUsers(ImportRequest request) {
        // Batch operations restricted to HTTP only
        return userService.bulkImport(request);
    }
}
```

## The Power of Protocol Aggregation

```java
public class Application {
    public static void main(String[] args) {
        ProtocolAggregator aggregator = new ProtocolAggregator();

        // Register multiple protocols
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        // Future: aggregator.registerProtocol(new GrpcProtocol(), new GrpcFoyer(9090));

        // Business logic written ONCE
        aggregator.registerConductor(new UserConductor());
        aggregator.registerConductor(new OrderConductor());
        aggregator.registerConductor(new PaymentConductor());

        aggregator.start();
    }
}
```

## Why Horizon?

### Before Horizon (Protocol Hell)
```java
@RestController
public class UserRestController {
    @PostMapping("/users")
    public User createUser(@RequestBody UserDto dto) {
        // Duplicate logic #1
    }
}

@MessageMapping("/user.create")
public class UserWebSocketHandler {
    public User handleCreate(UserDto dto) {
        // Duplicate logic #2
    }
}

@GrpcService
public class UserGrpcService {
    public User createUser(CreateUserRequest request) {
        // Duplicate logic #3
    }
}
```

### With Horizon (Unified & Secure)
```java
@Conductor(namespace = "user")
@ProtocolAccess({"HTTP", "WebSocket", "gRPC"})
public class UserConductor {
    
    @Intent("create")
    @ProtocolMapping(protocol = "HTTP", mapping = {"POST /users"})
    @ProtocolMapping(protocol = "WebSocket", mapping = {"user.create"})
    @ProtocolMapping(protocol = "gRPC", mapping = {"UserService.CreateUser"})
    public User createUser(CreateUserRequest request) {
        // Logic written ONCE, works everywhere with security!
        return userService.createUser(request);
    }
}
```

## Project Status

Version 0.4 is a complete rewrite focusing on:
- **Simplicity**: Easier to understand and use
- **Protocol Aggregation**: True multi-protocol support
- **Security**: Fine-grained protocol access control
- **Performance**: Minimal overhead
- **Extensibility**: Easy to add new protocols

### Current Progress
- ✅ Core Framework
- ✅ HTTP Protocol Support
- 🚧 WebSocket Protocol Support
- 📋 gRPC Protocol (Planned)
- 📋 GraphQL Protocol (Planned)

## Getting Started

```bash
# Clone the repository
git clone https://github.com/yourusername/horizon-framework.git

# Build the project
./gradlew build
```

## Key Features

### 🔒 Protocol Access Control
Control which protocols can access your business logic:

```java
@Conductor(namespace = "admin")
@ProtocolAccess({"HTTP"})  // Admin operations only via HTTP
public class AdminConductor {
    // ...
}

@Conductor(namespace = "chat")
@ProtocolAccess({"WebSocket"})  // Real-time features only via WebSocket
public class ChatConductor {
    // ...
}
```

### 🎯 Protocol-Neutral Routing
Define routes in a protocol-agnostic way:

```java
@Intent("create")
@ProtocolMapping(protocol = "HTTP", mapping = {"POST /users"})
@ProtocolMapping(protocol = "WebSocket", mapping = {"user.create"})
@ProtocolMapping(protocol = "gRPC", mapping = {"UserService.CreateUser"})
public User createUser(CreateUserRequest request) {
    // Same logic, multiple protocols
}
```

## Architecture

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ HTTP Foyer  │  │  WS Foyer   │  │ gRPC Foyer  │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                 │                 │
       └─────────────────┴─────────────────┘
                         │
                   ┌─────┴─────┐
                   │Rendezvous │ (All protocols meet here)
                   └─────┬─────┘
                         │
                 ┌───────┴────────┐
                 │  Conductors    │ (Your business logic)
                 └────────────────┘
```

## Contributing

Horizon is in active development. We welcome contributions!

## License

MIT License
