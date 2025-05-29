# Horizon Framework - gRPC Support Guide

## Overview

Horizon Framework v0.5 now supports gRPC (Google Remote Procedure Call) protocol, enabling high-performance, strongly-typed communication alongside HTTP and WebSocket. This guide explains how to use gRPC with Horizon.

## Key Features

- **Protocol Unification**: Same business logic accessible via HTTP, WebSocket, and gRPC
- **Automatic Intent Mapping**: gRPC service methods automatically mapped to Horizon intents
- **Type Safety**: Full Protocol Buffers support with automatic conversion
- **Streaming Support**: Unary, server streaming, client streaming, and bidirectional streaming
- **Interceptor Support**: gRPC interceptors integrate with Horizon's pipeline

## Quick Start

### 1. Add Dependencies

```gradle
dependencies {
    implementation 'horizon:horizon-core:0.5.0'
    implementation 'horizon:horizon-web:0.5.0'
    
    // gRPC dependencies
    implementation 'io.grpc:grpc-netty-shaded:1.58.0'
    implementation 'io.grpc:grpc-protobuf:1.58.0'
    implementation 'io.grpc:grpc-stub:1.58.0'
}
```

### 2. Define Your Service (Optional)

You can use standard Protocol Buffers definitions:

```protobuf
syntax = "proto3";

service UserService {
  rpc CreateUser(CreateUserRequest) returns (User);
  rpc GetUser(GetUserRequest) returns (User);
}

message User {
  int64 id = 1;
  string name = 2;
  string email = 3;
}
```

### 3. Create a Multi-Protocol Conductor

```java
@Conductor(namespace = "user")
public class UserConductor {
    
    @Intent("create")
    @ProtocolAccess(
        schema = {
            @ProtocolSchema(protocol = "HTTP", value = "POST /users"),
            @ProtocolSchema(protocol = "WebSocket", value = "user.create"),
            @ProtocolSchema(protocol = "gRPC", value = "UserService.CreateUser")
        }
    )
    public User createUser(@RequestBody CreateUserRequest request) {
        // Same logic for all protocols!
        return userService.create(request);
    }
}
```

### 4. Configure the Aggregator

```java
public class Application {
    public static void main(String[] args) {
        ProtocolAggregator aggregator = new ProtocolAggregator();
        
        // Register protocols
        aggregator.registerProtocol(new HttpProtocol(), new HttpFoyer(8080));
        aggregator.registerProtocol(new WebSocketProtocol(), new WebSocketFoyer(8081));
        aggregator.registerProtocol(new GrpcProtocol(), new GrpcFoyer(9090));
        
        // Scan conductors
        aggregator.scanConductors("com.example.conductors");
        
        // Start
        aggregator.start();
    }
}
```

## Intent Mapping

### Automatic Mapping

gRPC service/method names are automatically converted to Horizon intents:

| gRPC Method | Horizon Intent |
|-------------|----------------|
| UserService/CreateUser | user.create |
| UserService/GetUser | user.get |
| UserService/UpdateUser | user.update |
| ProductService/ListProducts | product.list |

### Custom Mapping

You can override the default mapping:

```java
@Intent("custom.intent")
@ProtocolAccess(
    schema = @ProtocolSchema(
        protocol = "gRPC", 
        value = "CustomService.SpecialMethod"
    )
)
public Result customMethod(@RequestBody Request request) {
    // ...
}
```

## Type Conversion

### Protocol Buffers → Java Objects

gRPC messages are automatically converted to Java objects:

```java
// Proto definition
message CreateUserRequest {
    string name = 1;
    string email = 2;
}

// Conductor method - works with Map or DTO
@Intent("create")
public User createUser(@RequestBody Map<String, Object> request) {
    String name = (String) request.get("name");
    String email = (String) request.get("email");
    // ...
}

// Or with typed DTO
@Intent("create")
public User createUser(@RequestBody CreateUserDto request) {
    // Automatic conversion from protobuf
}
```

### Java Objects → Protocol Buffers

Return values are automatically converted to Protocol Buffers:

```java
@Intent("get")
public Map<String, Object> getUser(@PathParam("id") Long id) {
    // Return a Map - automatically converted to protobuf
    return Map.of(
        "id", id,
        "name", "John Doe",
        "email", "john@example.com"
    );
}
```

## Advanced Features

### 1. Streaming Support

```java
@Intent("stream")
@ProtocolAccess(
    schema = @ProtocolSchema(
        protocol = "gRPC",
        value = "StreamService.StreamData",
        attributes = {"streaming", "server"}
    )
)
public Flux<Data> streamData(@RequestBody StreamRequest request) {
    // Return reactive stream for server streaming
    return dataService.streamData(request.getCriteria());
}
```

### 2. Metadata/Headers

```java
@Intent("secure")
public Result secureOperation(
    @Header("authorization") String token,
    @RequestBody Request request
) {
    // Headers from gRPC metadata are available
}
```

### 3. Error Handling

```java
@Intent("validate")
public Result validate(@RequestBody Request request) {
    if (!isValid(request)) {
        // Automatically converted to gRPC status
        throw new IllegalArgumentException("Invalid request");
    }
    return process(request);
}
```

Exception to gRPC Status mapping:
- `IllegalArgumentException` → `INVALID_ARGUMENT`
- `SecurityException` → `PERMISSION_DENIED`
- `UnsupportedOperationException` → `UNIMPLEMENTED`
- Others → `INTERNAL`

### 4. Custom Configuration

```java
GrpcConfiguration config = GrpcConfiguration.defaultConfig()
    .setMaxInboundMessageSize(10 * 1024 * 1024)  // 10MB
    .setCompressionEnabled(true)
    .addInterceptor(new AuthenticationInterceptor())
    .addInterceptor(new LoggingInterceptor());

GrpcFoyer grpcFoyer = new GrpcFoyer(9090, config);
```

## Best Practices

### 1. Use Typed DTOs When Possible

```java
// Good - type safety
@Intent("create")
public User createUser(@RequestBody CreateUserRequest request) {
    return userService.create(request);
}

// Less ideal - manual type checking
@Intent("create")
public User createUser(@RequestBody Map<String, Object> request) {
    String name = (String) request.get("name");  // Manual casting
}
```

### 2. Consistent Naming

Follow consistent naming patterns for better intent mapping:

```protobuf
service UserService {
    rpc CreateUser(...) returns (...);    // → user.create
    rpc GetUser(...) returns (...);       // → user.get
    rpc UpdateUser(...) returns (...);    // → user.update
    rpc DeleteUser(...) returns (...);    // → user.delete
    rpc ListUsers(...) returns (...);     // → user.list
}
```

### 3. Error Messages

Provide clear error messages that work well across all protocols:

```java
@Intent("create")
public User createUser(@RequestBody CreateUserRequest request) {
    if (request.getEmail() == null) {
        // This message appears in HTTP 400, WebSocket error, and gRPC status
        throw new IllegalArgumentException("Email is required");
    }
}
```

## Performance Considerations

1. **Binary Protocol**: gRPC uses Protocol Buffers (binary) - more efficient than JSON
2. **HTTP/2**: Built on HTTP/2 with multiplexing and streaming
3. **Type Safety**: Compile-time type checking reduces runtime errors
4. **Compression**: Built-in compression support

## Migration from Pure gRPC

If you have existing gRPC services, you can gradually migrate:

```java
// Step 1: Add Horizon annotations to existing service
@Conductor(namespace = "user")
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    
    @Override
    @Intent("create")
    @ProtocolAccess(schema = @ProtocolSchema(protocol = "gRPC", value = "UserService.CreateUser"))
    public void createUser(CreateUserRequest request, StreamObserver<User> observer) {
        // Existing gRPC logic
    }
}

// Step 2: Refactor to Horizon style
@Intent("create")
@ProtocolAccess({"HTTP", "WebSocket", "gRPC"})
public User createUser(@RequestBody CreateUserRequest request) {
    // Now accessible via all protocols!
}
```

## Limitations

1. **Generic Types**: Currently requires manual type mapping for complex generic types
2. **Streaming**: Full streaming support requires additional configuration
3. **Proto Generation**: Proto file generation from Horizon annotations not yet supported

## Summary

gRPC support in Horizon Framework provides:
- **Unified Business Logic**: One implementation, three protocols
- **Type Safety**: Full Protocol Buffers support
- **Performance**: Binary protocol with HTTP/2
- **Flexibility**: Mix and match protocols as needed

The same conductor can now serve REST APIs, real-time WebSocket connections, and high-performance gRPC calls - all with a single implementation!