package horizon.web.grpc;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Converts between Protocol Buffer messages and Java objects.
 * Supports both static (compiled) and dynamic messages.
 */
public class GrpcMessageConverter {
    
    private static final JsonFormat.Parser JSON_PARSER = JsonFormat.parser()
            .ignoringUnknownFields();
    private static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer()
            .includingDefaultValueFields()
            .preservingProtoFieldNames();
    
    // Cache for message descriptors
    private final Map<String, Descriptors.Descriptor> descriptorCache = new ConcurrentHashMap<>();
    
    // Cache for message parsers
    private final Map<Class<? extends Message>, Message.Builder> builderCache = new ConcurrentHashMap<>();
    
    /**
     * Converts a Protocol Buffer message to a Java Map.
     * 
     * @param message the Protocol Buffer message
     * @return the converted Map
     * @throws InvalidProtocolBufferException if conversion fails
     */
    public Map<String, Object> messageToMap(Message message) throws InvalidProtocolBufferException {
        String json = JSON_PRINTER.print(message);
        return parseJsonToMap(json);
    }
    
    /**
     * Converts a Java object to a Protocol Buffer message.
     * 
     * @param data the data to convert
     * @param messageClass the target message class
     * @return the converted message
     * @throws Exception if conversion fails
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> T objectToMessage(Object data, Class<T> messageClass) throws Exception {
        Message.Builder builder = getBuilder(messageClass);
        
        String json;
        if (data instanceof String) {
            json = (String) data;
        } else {
            json = horizon.core.util.JsonUtils.toJson(data);
        }
        
        JSON_PARSER.merge(json, builder);
        return (T) builder.build();
    }
    
    /**
     * Converts a ByteString to a Protocol Buffer message.
     * 
     * @param bytes the ByteString containing the serialized message
     * @param messageClass the target message class
     * @return the parsed message
     * @throws InvalidProtocolBufferException if parsing fails
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> T bytesToMessage(ByteString bytes, Class<T> messageClass) 
            throws InvalidProtocolBufferException {
        Message.Builder builder = getBuilder(messageClass);
        return (T) builder.mergeFrom(bytes).build();
    }
    
    /**
     * Converts a Protocol Buffer message to ByteString.
     * 
     * @param message the message to serialize
     * @return the serialized ByteString
     */
    public ByteString messageToBytes(Message message) {
        return message.toByteString();
    }
    
    /**
     * Creates a dynamic message from a descriptor and data.
     * 
     * @param descriptor the message descriptor
     * @param data the data to populate
     * @return the dynamic message
     * @throws Exception if creation fails
     */
    public DynamicMessage createDynamicMessage(Descriptors.Descriptor descriptor, Map<String, Object> data) 
            throws Exception {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        String json = horizon.core.util.JsonUtils.toJson(data);
        JSON_PARSER.merge(json, builder);
        return builder.build();
    }
    
    /**
     * Wraps any message in a google.protobuf.Any.
     * 
     * @param message the message to wrap
     * @return the Any message
     */
    public Any wrapInAny(Message message) {
        return Any.pack(message);
    }
    
    /**
     * Unwraps a google.protobuf.Any message.
     * 
     * @param any the Any message
     * @param messageClass the expected message class
     * @return the unwrapped message
     * @throws InvalidProtocolBufferException if unwrapping fails
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> T unwrapAny(Any any, Class<T> messageClass) 
            throws InvalidProtocolBufferException {
        return (T) any.unpack(messageClass);
    }
    
    /**
     * Registers a message descriptor for dynamic message handling.
     * 
     * @param fullName the full name of the message type
     * @param descriptor the message descriptor
     */
    public void registerDescriptor(String fullName, Descriptors.Descriptor descriptor) {
        descriptorCache.put(fullName, descriptor);
    }
    
    /**
     * Gets a cached message builder for the given class.
     * 
     * @param messageClass the message class
     * @return a new builder instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> Message.Builder getBuilder(Class<T> messageClass) {
        return builderCache.computeIfAbsent(messageClass, clazz -> {
            try {
                Message defaultInstance = (Message) clazz.getMethod("getDefaultInstance").invoke(null);
                return defaultInstance.toBuilder();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get builder for " + clazz.getName(), e);
            }
        }).clone();
    }
    
    /**
     * Gets a cached message builder for the given class (package private).
     * 
     * @param messageClass the message class
     * @return a new builder instance
     */
    @SuppressWarnings("unchecked")
    private <T extends Message> Message.Builder getBuilderInternal(Class<T> messageClass) {
        return builderCache.computeIfAbsent(messageClass, clazz -> {
            try {
                Message defaultInstance = (Message) clazz.getMethod("getDefaultInstance").invoke(null);
                return defaultInstance.toBuilder();
            } catch (Exception e) {
                throw new RuntimeException("Failed to get builder for " + clazz.getName(), e);
            }
        }).clone();
    }
    
    /**
     * Parses JSON string to Map.
     * 
     * @param json the JSON string
     * @return the parsed Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return horizon.core.util.JsonUtils.fromJson(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to Map", e);
        }
    }
}
