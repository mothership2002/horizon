# Horizon Framework: Architecture and Philosophy

## Overview

The Horizon Framework is a flexible, protocol-agnostic application framework designed to handle requests from various sources through a unified processing pipeline. It emphasizes modularity, extensibility, and separation of concerns, allowing developers to focus on business logic rather than infrastructure details.

## Core Philosophy

The Horizon Framework is built around several key philosophical principles:

1. **Protocol Agnosticism**: The framework can handle requests from any protocol (HTTP, WebSocket, gRPC, CLI, etc.) through a unified processing pipeline.

2. **Separation of Concerns**: Each component has a well-defined responsibility, making the system easier to understand, test, and maintain.

3. **Modularity**: Components can be replaced or extended independently, allowing for flexible customization.

4. **Extensibility**: The framework is designed to be extended with new protocols, processing steps, and business logic.

5. **Type Safety**: Extensive use of generics ensures type safety throughout the processing pipeline.

## Architecture

The Horizon Framework follows a layered architecture with clear separation of concerns:

### System Context Layer

At the highest level, the `HorizonSystemContext` acts as a registry for runtime units and foyers, managing the lifecycle of these components and providing a centralized point of access.

### Protocol Layer

The protocol layer handles the communication with external systems through protocol-specific implementations:

- **Protocol**: Defines the basic properties and lifecycle methods for a communication protocol.
- **ProtocolAdapter**: Converts between protocol-specific messages and Horizon's RawInput/RawOutput.
- **ProtocolFoyer**: Acts as a bridge between a protocol and the Rendezvous component.
- **Foyer**: Serves as an entry point for incoming requests, with methods for allowing or denying requests.

### Processing Pipeline Layer

The processing pipeline handles the transformation of raw input into meaningful output:

- **Rendezvous**: Creates a context from raw input and finalizes the output from a context.
- **Sentinel**: Inspects input and output for logging, validation, security checks, etc.
- **Normalizer**: Normalizes raw input into a standardized format.
- **Interpreter**: Extracts intent keys and payloads from normalized input.
- **Conductor**: Orchestrates the execution of commands based on intent.
- **StageHandler**: Handles commands at different stages of processing.

### Data Model Layer

The data model represents the state of a request as it flows through the system:

- **RawInput**: Represents the raw input data that comes into the system.
- **RawOutput**: Represents the raw output data that goes out of the system.
- **HorizonContext**: Holds the state of a request as it flows through the system.

### Engine Layer

The engine layer orchestrates the flow of data through the system:

- **HorizonFlowEngine**: Manages the execution of the processing pipeline.

## Data Flow

The typical flow of data through the Horizon Framework is as follows:

1. An external system sends a request to a protocol-specific endpoint.
2. The request is received by a `Foyer`, which acts as an entry point.
3. The `Foyer` checks if the request should be allowed and converts it to a `RawInput`.
4. The `RawInput` is passed to a `Rendezvous`, which creates a `HorizonContext`.
5. The `Rendezvous` processes the input through `Sentinel`s for inspection.
6. The `Normalizer` converts the raw input into a standardized format.
7. The `Interpreter` extracts the intent key and payload from the normalized input.
8. The `Conductor` orchestrates the execution of commands based on the intent.
9. The `StageHandler`s process the commands at different stages.
10. The result is stored in the `HorizonContext`.
11. The `Rendezvous` finalizes the output from the context.
12. The output is converted to a protocol-specific response and sent back to the external system.

## Protocol-Agnostic Design

The Horizon Framework's protocol-agnostic design is achieved through several key abstractions:

1. **Protocol Interface**: Defines the basic properties and lifecycle methods for a communication protocol.
2. **ProtocolAdapter Interface**: Converts between protocol-specific messages and Horizon's RawInput/RawOutput.
3. **ProtocolFoyer Abstract Class**: Acts as a bridge between a protocol and the Rendezvous component.
4. **Scheme Enum**: Defines the supported protocols in the framework.

This design allows the framework to work with various protocols without being tied to any specific implementation. New protocols can be added by implementing these interfaces and extending the Scheme enum.

## Extending the Framework

The Horizon Framework can be extended in several ways:

### Adding a New Protocol

To add a new protocol, you need to:

1. Create a class that implements the `Protocol` interface.
2. Create a class that implements the `ProtocolAdapter` interface.
3. Create a class that extends the `ProtocolFoyer` abstract class.
4. Add a new value to the `Scheme` enum.
5. Register the protocol foyer with the `HorizonSystemContext`.

### Adding a New Processing Step

To add a new processing step, you can:

1. Create a new interface that defines the step's behavior.
2. Create implementations of the interface for different use cases.
3. Integrate the step into the processing pipeline by modifying the `AbstractRendezvous` class or creating a new subclass.

### Adding New Business Logic

To add new business logic, you can:

1. Create a new `Command` implementation that encapsulates the logic.
2. Create a new `Conductor` implementation that orchestrates the execution of the command.
3. Create a new `StageHandler` implementation that handles the command at a specific stage.
4. Register the conductor and stage handler with a `HorizonRuntimeUnit`.

## Best Practices

When working with the Horizon Framework, consider the following best practices:

1. **Use Dependency Injection**: Inject dependencies rather than creating them directly to improve testability and flexibility.
2. **Follow the Single Responsibility Principle**: Each class should have a single responsibility and reason to change.
3. **Use Generics Appropriately**: Leverage the framework's generic type system to ensure type safety.
4. **Handle Errors Gracefully**: Implement proper error handling at each stage of the processing pipeline.
5. **Write Unit Tests**: Test each component in isolation to ensure it behaves as expected.
6. **Document Your Code**: Provide clear documentation for your extensions and customizations.
7. **Use Logging**: Leverage the framework's logging capabilities to aid in debugging and monitoring.

## Conclusion

The Horizon Framework provides a flexible, protocol-agnostic foundation for building applications that can handle requests from various sources through a unified processing pipeline. By following the framework's philosophy and best practices, you can create maintainable, extensible applications that focus on business logic rather than infrastructure details.