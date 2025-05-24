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
// Your business logic - written once
@Conductor("user.create")
public class CreateUserConductor implements Conductor<UserData, User> {
    public User conduct(UserData data) {
        return userService.createUser(data);
    }
}

// Works with ALL protocols automatically!
// HTTP POST /api/users
// WebSocket message: {"intent": "user.create", "data": {...}}
// gRPC: CreateUserRequest
```

## Project Status

Version 0.4 is a complete rewrite focusing on:
- **Simplicity**: Easier to understand and use
- **Protocol Aggregation**: True multi-protocol support
- **Performance**: Minimal overhead
- **Extensibility**: Easy to add new protocols

## Getting Started

*Documentation coming soon...*
