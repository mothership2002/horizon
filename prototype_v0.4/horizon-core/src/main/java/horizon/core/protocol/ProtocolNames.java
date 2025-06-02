package horizon.core.protocol;

/**
 * Constants for built-in protocol names.
 * Use these constants when specifying protocols in annotations.
 */
public final class ProtocolNames {
    // Web protocols
    public static final String HTTP = "HTTP";
    public static final String WEBSOCKET = "WebSocket";
    
    // Future protocols (not yet implemented)
    // public static final String GRAPHQL = "GraphQL";
    // public static final String MQTT = "MQTT";
    // public static final String AMQP = "AMQP";
    
    // Prevent instantiation
    private ProtocolNames() {}
}
