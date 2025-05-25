# Horizon Framework Architecture

## Overview

Horizon is a protocol aggregation framework designed for the multi-protocol era. It allows applications to handle multiple protocols (REST, WebSocket, gRPC, GraphQL) with a single business logic implementation. This document provides an overview of the architecture and key components of the Horizon Framework.

## Core Concepts

### Rendezvous

The Rendezvous is the central meeting point where all protocols converge. No matter how a request arrives, it meets your business logic at the Rendezvous. The Rendezvous is responsible for:

1. **Encountering** requests from any protocol
2. **Normalizing** them into a common context
3. **Routing** them to the appropriate Conductor
4. **Falling away** with a response adapted for the originating protocol

### Conductor

A Conductor orchestrates the handling of specific intents. It interprets the intent and payload, conducting the appropriate action. Conductors are the heart of your business logic and can be implemented in two ways:

1. **Interface-based**: Implementing the `Conductor<P, R>` interface
2. **Annotation-based**: Using `@Conductor`, `@Intent`, and protocol mapping annotations

Conductors are protocol-agnostic, meaning they don't need to know which protocol a request came from. They focus solely on business logic.

### Foyer

A Foyer is the entry point for a specific protocol. It's where protocol-specific requests first arrive before meeting at the Rendezvous. Each protocol has its own Foyer that:

1. **Accepts** protocol-specific requests
2. **Forwards** them to the Rendezvous
3. **Returns** protocol-specific responses

### Protocol Adapter

A Protocol Adapter bridges between diverse protocols and the unified Horizon processing. It:

1. **Extracts** intent and payload from protocol-specific requests
2. **Builds** protocol-specific responses from processing results

## Architecture Flow

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

1. A request arrives at a protocol-specific Foyer
2. The Foyer forwards the request to the Rendezvous
3. The Rendezvous uses a Protocol Adapter to extract the intent and payload
4. The Rendezvous finds the appropriate Conductor for the intent
5. The Conductor processes the request and returns a result
6. The Rendezvous uses the Protocol Adapter to build a protocol-specific response
7. The response is returned to the client through the Foyer

## Protocol Aggregator

The Protocol Aggregator is the heart of the Horizon Framework. It:

1. **Registers** protocols and their Foyers
2. **Registers** conductors for handling specific intents
3. **Coordinates** the flow of requests through the system
4. **Enforces** protocol access control

## Security Features

### Protocol Access Control

Horizon provides fine-grained control over which protocols can access which conductors or intent methods:

```java
@Conductor(namespace = "admin")
@ProtocolAccess({"HTTP"})  // Admin operations only via HTTP
public class AdminConductor {
    // ...
}

@Intent("bulkCreate")
@HttpResource("POST /users/bulk-create")  // Only HTTP has mapping = only HTTP can access
public BulkCreateUserResponse bulkCreateUsers(BulkCreateUserRequest request) {
    // ...
}
```

## Protocol-Neutral Routing

Horizon allows defining routes in a protocol-agnostic way:

```java
@Intent("create")
@HttpResource("POST /users")
@WebSocketResource("user.create")
public User createUser(CreateUserRequest request) {
    // Same logic, multiple protocols
}
```

## Conclusion

The Horizon Framework provides a clean, declarative, and powerful way to handle multiple protocols with a single business logic implementation. Its architecture is designed to be simple, secure, and extensible, making it ideal for modern applications that need to speak multiple protocols.