# Protocol Integration in Horizon Framework

This document explains how to integrate different protocols with the Horizon framework using the protocol-agnostic architecture.

## Overview

The Horizon framework now supports a protocol-agnostic approach to handling different communication protocols. This allows the framework to work with various protocols (HTTP, WebSocket, TCP, etc.) without being tied to any specific implementation.

The key components of this architecture are:

1. **Protocol** - Represents a communication protocol with basic properties and lifecycle methods
2. **ProtocolAdapter** - Converts between protocol-specific messages and Horizon's RawInput/RawOutput
3. **ProtocolFoyer** - Acts as a bridge between a protocol and the Rendezvous component

## How to Integrate a New Protocol

To integrate a new protocol with the Horizon framework, follow these steps:

### 1. Create a Protocol Implementation

Create a class that implements the `Protocol` interface:

```java
public class MyProtocol implements Protocol {
    private static final String PROTOCOL_NAME = "myprotocol";
    private static final int DEFAULT_PORT = 8082;
    
    private boolean initialized = false;

    @Override
    public String getName() {
        return PROTOCOL_NAME;
    }

    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public void initialize() {
        initialized = true;
    }

    @Override
    public void shutdown() {
        initialized = false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }
}
```

### 2. Create a Protocol Adapter

Create a class that implements the `ProtocolAdapter` interface:

```java
public class MyProtocolAdapter<I extends RawInput, O extends RawOutput> 
        implements ProtocolAdapter<I, O, MyMessage, MyResponse> {
    
    private final MyInputConverter<I> inputConverter;

    public MyProtocolAdapter(MyInputConverter<I> inputConverter) {
        this.inputConverter = Objects.requireNonNull(inputConverter);
    }

    @Override
    public I convertToInput(MyMessage message, String remoteAddress) {
        return inputConverter.convert(message, remoteAddress);
    }

    @Override
    public MyResponse convertToResponse(O output, Object context) {
        // Convert Horizon output to protocol-specific response
        return new MyResponse(output.toString());
    }

    @Override
    public MyResponse createErrorResponse(Throwable e, Object context) {
        // Create an error response
        return new MyResponse("Error: " + e.getMessage());
    }

    @Override
    public MyResponse createForbiddenResponse(Object context) {
        // Create a forbidden response
        return new MyResponse("Forbidden");
    }
}
```

### 3. Create a Protocol Foyer

Create a class that extends the `ProtocolFoyer` abstract class:

```java
public class MyProtocolFoyer<I extends RawInput, O extends RawOutput> 
        extends ProtocolFoyer<I, O, MyMessage, MyResponse> {
    
    private MyServer server;

    public MyProtocolFoyer(int port, Rendezvous<I, O> rendezvous, MyProtocolAdapter<I, O> adapter) {
        super(port, rendezvous, adapter, new MyProtocol());
    }

    @Override
    protected void initializeServer() throws Exception {
        // Initialize the server for this protocol
        server = new MyServer(getPort());
        server.setMessageHandler((message, remoteAddress) -> {
            // Handle the message using the ProtocolFoyer's handleMessage method
            MyResponse response = handleMessage(message, remoteAddress, null);
            // Send the response back to the client
            server.sendResponse(response);
        });
        server.start();
    }

    @Override
    protected void shutdownServer() throws Exception {
        // Shutdown the server for this protocol
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
```

### 4. Register the Protocol Foyer

Register the protocol foyer with the `HorizonSystemContext`:

```java
// Create the input converter
MyInputConverter<MyInput> inputConverter = new MyInputConverter<>();

// Create the adapter
MyProtocolAdapter<MyInput, MyOutput> adapter = new MyProtocolAdapter<>(inputConverter);

// Create the foyer
MyProtocolFoyer<MyInput, MyOutput> foyer = new MyProtocolFoyer<>(8082, rendezvous, adapter);

// Register the foyer with the system context
systemContext.registerFoyer(Scheme.valueOf("MYPROTOCOL"), foyer);
```

Alternatively, you can use the `createAndRegisterFoyer` method:

```java
MyProtocolFoyer<MyInput, MyOutput> foyer = systemContext.createAndRegisterFoyer(
    Scheme.valueOf("MYPROTOCOL"),
    rendezvous -> new MyProtocolFoyer<>(8082, rendezvous, new MyProtocolAdapter<>(new MyInputConverter<>()))
);
```

## Example: HTTP Protocol

The Horizon framework includes an implementation of the HTTP protocol using Netty:

```java
// Create the input converter
NettyInputConverter<HttpInput> inputConverter = new HttpInputConverter();

// Create and register the HTTP foyer
NettyHttpFoyer<HttpInput, HttpOutput> foyer = systemContext.createAndRegisterNettyHttpFoyer(
    Scheme.HTTP,
    8080,
    inputConverter
);
```

## Example: WebSocket Protocol

The Horizon framework includes an implementation of the WebSocket protocol:

```java
// Create the input converter
WebSocketInputConverter<WsInput> inputConverter = new WebSocketInputConverter();

// Create the adapter
WebSocketAdapter<WsInput, WsOutput> adapter = new WebSocketAdapter<>(inputConverter);

// Create and register the WebSocket foyer
WebSocketFoyer<WsInput, WsOutput> foyer = systemContext.createAndRegisterFoyer(
    Scheme.WS,
    rendezvous -> new WebSocketFoyer<>(8081, rendezvous, adapter)
);
```

## Benefits of the Protocol-Agnostic Approach

1. **Flexibility** - The framework can work with any protocol without being tied to a specific implementation
2. **Modularity** - Protocol-specific code is isolated in separate classes
3. **Extensibility** - New protocols can be added without modifying the core framework
4. **Testability** - Protocol implementations can be tested independently
5. **Maintainability** - Changes to one protocol don't affect others