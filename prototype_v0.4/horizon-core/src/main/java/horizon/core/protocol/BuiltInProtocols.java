package horizon.core.protocol;

/**
 * Built-in protocols supported by Horizon Framework.
 * Using enum for compile-time safety and standardization.
 */
public enum BuiltInProtocols implements Protocol {
    HTTP("HTTP", "HyperText Transfer Protocol"),
    WEBSOCKET("WebSocket", "WebSocket Protocol"),
    GRPC("gRPC", "gRPC Remote Procedure Call"),
    GRAPHQL("GraphQL", "GraphQL Query Language"),
    MQTT("MQTT", "Message Queuing Telemetry Transport"),
    AMQP("AMQP", "Advanced Message Queuing Protocol");
    
    private final String name;
    private final String displayName;
    
    BuiltInProtocols(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
}
