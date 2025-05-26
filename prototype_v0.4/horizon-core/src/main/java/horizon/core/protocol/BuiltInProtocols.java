package horizon.core.protocol;

/**
 * Constants for built-in protocol names.
 * Use these constants when specifying protocols in annotations.
 */
public final class ProtocolNames {
    // Web protocols
    public static final String HTTP = "HTTP";
    public static final String WEBSOCKET = "WebSocket";
    
    // RPC protocols
    public static final String GRPC = "gRPC";
    public static final String GRAPHQL = "GraphQL";
    
    // Messaging protocols
    public static final String MQTT = "MQTT";
    public static final String AMQP = "AMQP";
    
    // Prevent instantiation
    private ProtocolNames() {}
}
