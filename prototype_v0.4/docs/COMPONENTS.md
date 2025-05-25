# Horizon Framework Components

This document provides detailed descriptions of the key components in the Horizon Framework.

## Core Components

### Protocol

The `Protocol` interface defines a protocol that can be used in the Horizon Framework. It's a marker interface that all protocol definitions must implement. It provides methods for getting the name and display name of the protocol.

```java
public interface Protocol {
    String getName();
    default String getDisplayName() {
        return getName();
    }
}
```

Implementations:
- `HttpProtocol`: Implements the Protocol interface for HTTP
- `WebSocketProtocol`: Implements the Protocol interface for WebSocket

### ProtocolAdapter

The `ProtocolAdapter<I, O>` interface adapts protocol-specific requests and responses to the Horizon format. It's the bridge between diverse protocols and the unified Horizon processing.

```java
public interface ProtocolAdapter<I, O> {
    String extractIntent(I request);
    Object extractPayload(I request);
    O buildResponse(Object result, I request);
    O buildErrorResponse(Throwable error, I request);
}
```

Implementations:
- `HttpProtocolAdapter`: Adapts HTTP requests and responses
- `WebSocketProtocolAdapter`: Adapts WebSocket messages

### Conductor

The `Conductor<P, R>` interface orchestrates the handling of specific intents. It interprets the intent and payload, conducting the appropriate action.

```java
public interface Conductor<P, R> {
    R conduct(P payload);
    String getIntentPattern();
}
```

Implementations:
- `AbstractConductor<P, R>`: A base implementation of the Conductor interface
- Annotation-based conductors: Classes annotated with `@Conductor`

### Foyer

The `Foyer<I>` interface is the entry point for a specific protocol. It's where protocol-specific requests first arrive before meeting at the Rendezvous.

```java
public interface Foyer<I> {
    void open();
    void close();
    boolean isOpen();
    void connectToRendezvous(Rendezvous<I, ?> rendezvous);
}
```

Implementations:
- `HttpFoyer`: Implements the Foyer interface for HTTP
- `WebSocketFoyer`: Implements the Foyer interface for WebSocket

### Rendezvous

The `Rendezvous<I, O>` interface is the central meeting point where all protocols converge. It's responsible for encountering requests and falling away with responses.

```java
public interface Rendezvous<I, O> {
    HorizonContext encounter(I input);
    O fallAway(HorizonContext context);
}
```

Implementations:
- `ProtocolSpecificRendezvous<I, O>`: Adapts protocol-specific requests to the central rendezvous

### ProtocolAggregator

The `ProtocolAggregator` class is the heart of the Horizon Framework. It aggregates multiple protocols into a unified processing pipeline.

```java
public class ProtocolAggregator {
    public <I, O> void registerProtocol(Protocol<I, O> protocol, Foyer<I> foyer);
    public void registerConductor(Conductor<?, ?> conductor);
    public void scanConductors(String basePackage);
    public void start();
    public void stop();
}
```

## Annotations

### @Conductor

The `@Conductor` annotation marks a class as a conductor for handling specific intents. It can specify a namespace for all intents in the class.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conductor {
    String namespace() default "";
}
```

### @Intent

The `@Intent` annotation marks a method as an intent handler. It specifies the intent name and optional aliases.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Intent {
    String value();
    String[] aliases() default {};
}
```

### @ProtocolAccess

The `@ProtocolAccess` annotation specifies which protocols can access a conductor or intent method.

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtocolAccess {
    String[] value();
    boolean allowOthers() default false;
}
```

### @HttpResource

The `@HttpResource` annotation maps an HTTP endpoint to an intent method.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpResource {
    String value();
}
```

### @WebSocketResource

The `@WebSocketResource` annotation maps a WebSocket message to an intent method.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketResource {
    String value();
}
```

## Protocol-Specific Components

### HTTP Components

- `HttpProtocol`: Implements the Protocol interface for HTTP
- `HttpFoyer`: Implements the Foyer interface for HTTP
- `HttpProtocolAdapter`: Adapts HTTP requests and responses

### WebSocket Components

- `WebSocketProtocol`: Implements the Protocol interface for WebSocket
- `WebSocketFoyer`: Implements the Foyer interface for WebSocket
- `WebSocketProtocolAdapter`: Adapts WebSocket messages
- `WebSocketMessage`: Represents a WebSocket message with intent and data

## Security Components

### ProtocolAccessValidator

The `ProtocolAccessValidator` class validates whether a protocol has access to a specific method based on annotations.

```java
public class ProtocolAccessValidator {
    public boolean hasAccess(String protocolName, Method method);
}
```

## Utility Components

### ConductorScanner

The `ConductorScanner` class scans for classes annotated with `@Conductor` and registers them with the ProtocolAggregator.

```java
public class ConductorScanner {
    public void scan(String basePackage, ProtocolAggregator aggregator);
}
```

### ConductorRegistry

The `ConductorRegistry` class maintains a registry of conductors and provides methods for finding the appropriate conductor for an intent.

```java
public class ConductorRegistry {
    public void register(Conductor<?, ?> conductor);
    public Conductor<?, ?> find(String intent);
}
```

## Conclusion

The Horizon Framework is built around these key components, which work together to provide a unified way to handle multiple protocols with a single business logic implementation. The architecture is designed to be simple, secure, and extensible, making it ideal for modern applications that need to speak multiple protocols.