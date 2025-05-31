package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Defines a custom protocol that can be used in the application.
 * This allows extending beyond the built-in protocols (HTTP, WebSocket, gRPC).
 * 
 * Note: This annotation is for documentation purposes. The actual protocol
 * registration happens through {@link horizon.core.ProtocolAggregator#registerProtocol}.
 * 
 * Example:
 * <pre>
 * @CustomProtocol(
 *     name = "MQTT",
 *     displayName = "MQTT Protocol",
 *     description = "Message Queuing Telemetry Transport for IoT",
 *     adapterClass = MqttProtocolAdapter.class
 * )
 * public class MqttProtocol implements Protocol<MqttMessage, MqttMessage> {
 *     // Implementation
 * }
 * </pre>
 * 
 * @see horizon.core.protocol.Protocol
 * @see horizon.core.protocol.ProtocolAdapter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomProtocol {
    /**
     * The unique name of the protocol.
     * This is used in @ProtocolAccess annotations.
     */
    String name();
    
    /**
     * Display name of the protocol.
     * Used in logging and user interfaces.
     */
    String displayName() default "";
    
    /**
     * Description of the protocol.
     * Helps developers understand when to use this protocol.
     */
    String description() default "";
    
    /**
     * The adapter class that handles this protocol.
     * Must implement {@link horizon.core.protocol.ProtocolAdapter}.
     */
    Class<? extends horizon.core.protocol.ProtocolAdapter> adapterClass();
}
