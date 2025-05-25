# Horizon Framework Documentation

This directory contains documentation for the Horizon Framework, a protocol aggregation framework for the multi-protocol era.

## Documentation Files

### [ARCHITECTURE.md](ARCHITECTURE.md)

This document provides an overview of the architecture and key components of the Horizon Framework. It covers:

- Core concepts (Rendezvous, Conductor, Foyer, Protocol Adapter)
- Architecture flow
- Protocol Aggregator
- Security features
- Protocol-neutral routing

### [COMPONENTS.md](COMPONENTS.md)

This document provides detailed descriptions of the key components in the Horizon Framework. It covers:

- Core components (Protocol, ProtocolAdapter, Conductor, Foyer, Rendezvous, ProtocolAggregator)
- Annotations (@Conductor, @Intent, @ProtocolAccess, @HttpResource, @WebSocketResource)
- Protocol-specific components (HTTP and WebSocket)
- Security components (ProtocolAccessValidator)
- Utility components (ConductorScanner, ConductorRegistry)

### [USAGE_EXAMPLES.md](USAGE_EXAMPLES.md)

This document provides examples of how to use the Horizon Framework in your applications. It covers:

- Basic setup
- Creating conductors (interface-based and annotation-based)
- Protocol access control
- Method-level protocol access
- Intent aliases
- HTTP protocol examples
- WebSocket protocol examples

### [IMPROVEMENT_POINTS.md](IMPROVEMENT_POINTS.md)

This document outlines potential areas for improvement in the Horizon Framework. It covers:

- Current issues (type mismatch in Protocol interface, missing packages)
- Potential improvements (documentation, architecture, features, testing, performance, developer experience, compatibility)
- Roadmap (short-term, medium-term, long-term)

## Getting Started

If you're new to the Horizon Framework, we recommend starting with the [ARCHITECTURE.md](ARCHITECTURE.md) document to get an overview of the framework, then moving on to the [COMPONENTS.md](COMPONENTS.md) document to understand the key components in more detail. After that, check out the [USAGE_EXAMPLES.md](USAGE_EXAMPLES.md) document to see how to use the framework in your applications.

## Contributing

If you're interested in contributing to the Horizon Framework, check out the [IMPROVEMENT_POINTS.md](IMPROVEMENT_POINTS.md) document to see areas where the framework could be improved.