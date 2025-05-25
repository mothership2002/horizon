package horizon.demo.protocol;

import horizon.core.annotation.CustomProtocol;
import horizon.core.protocol.Protocol;
import horizon.core.protocol.ProtocolAdapter;

/**
 * Example of defining a custom protocol using annotation.
 */
@CustomProtocol(
    name = "MQTT",
    displayName = "MQTT Protocol",
    description = "Message Queuing Telemetry Transport for IoT devices",
    adapterClass = MqttProtocolAdapter.class
)
public class CustomMqttProtocol implements Protocol {
    
    @Override
    public String getName() {
        return "MQTT";
    }
    
    @Override
    public String getDisplayName() {
        return "MQTT Protocol";
    }
}

class MqttProtocolAdapter implements ProtocolAdapter<Object, Object> {
    @Override
    public String extractIntent(Object request) {
        // MQTT-specific logic
        return null;
    }
    
    @Override
    public Object extractPayload(Object request) {
        return null;
    }
    
    @Override
    public Object buildResponse(Object result, Object request) {
        return null;
    }
    
    @Override
    public Object buildErrorResponse(Throwable error, Object request) {
        return null;
    }
}
