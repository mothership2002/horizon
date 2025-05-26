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
public class CustomMqttProtocol implements Protocol<MqttMessage, MqttMessage> {
    
    @Override
    public String getName() {
        return "MQTT";
    }
    
    @Override
    public String getDisplayName() {
        return "MQTT Protocol";
    }
    
    @Override
    public ProtocolAdapter<MqttMessage, MqttMessage> createAdapter() {
        return new MqttProtocolAdapter();
    }
}

/**
 * Example MQTT message class.
 */
class MqttMessage {
    private String topic;
    private byte[] payload;
    private int qos;
    
    // Getters and setters
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public byte[] getPayload() {
        return payload;
    }
    
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
    
    public int getQos() {
        return qos;
    }
    
    public void setQos(int qos) {
        this.qos = qos;
    }
}

class MqttProtocolAdapter implements ProtocolAdapter<MqttMessage, MqttMessage> {
    @Override
    public String extractIntent(MqttMessage request) {
        // Extract intent from MQTT topic
        String topic = request.getTopic();
        if (topic != null) {
            // Convert MQTT topic to intent: devices/+/telemetry -> device.telemetry
            return topic.replace("/", ".").replace("+", "*");
        }
        return null;
    }
    
    @Override
    public Object extractPayload(MqttMessage request) {
        // Return the payload as-is or parse it
        return request.getPayload();
    }
    
    @Override
    public MqttMessage buildResponse(Object result, MqttMessage request) {
        MqttMessage response = new MqttMessage();
        response.setTopic(request.getTopic() + "/response");
        response.setQos(request.getQos());
        
        if (result instanceof byte[]) {
            response.setPayload((byte[]) result);
        } else if (result != null) {
            response.setPayload(result.toString().getBytes());
        }
        
        return response;
    }
    
    @Override
    public MqttMessage buildErrorResponse(Throwable error, MqttMessage request) {
        MqttMessage response = new MqttMessage();
        response.setTopic(request.getTopic() + "/error");
        response.setQos(request.getQos());
        response.setPayload(("Error: " + error.getMessage()).getBytes());
        return response;
    }
}
