package horizon.web.grpc;

import com.google.protobuf.Message;
import horizon.core.conductor.ConductorMethod;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for gRPC service methods and their corresponding message types.
 * This allows dynamic registration of gRPC endpoints without proto compilation.
 */
public class GrpcServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(GrpcServiceRegistry.class);

    // Map from intent to gRPC method info
    private final Map<String, GrpcMethodInfo> methodRegistry = new ConcurrentHashMap<>();

    // Map from full method name to message types
    private final Map<String, MessageTypePair> messageTypeRegistry = new ConcurrentHashMap<>();

    // Singleton instance
    private static final GrpcServiceRegistry INSTANCE = new GrpcServiceRegistry();

    private GrpcServiceRegistry() {
    }

    public static GrpcServiceRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a conductor method as a gRPC endpoint.
     *
     * @param conductorMethod the conductor method
     * @param serviceName     the gRPC service name
     * @param methodName      the gRPC method name
     */
    public void registerMethod(ConductorMethod conductorMethod, String serviceName, String methodName) {
        String intent = conductorMethod.getIntent();
        String fullMethodName = serviceName + "/" + methodName;

        // Try to infer message types from method signature
        MessageTypePair messageTypes = inferMessageTypes(conductorMethod);

        GrpcMethodInfo methodInfo = new GrpcMethodInfo(
                intent,
                serviceName,
                methodName,
                fullMethodName,
                MethodDescriptor.MethodType.UNARY, // Default to UNARY
                messageTypes
        );

        methodRegistry.put(intent, methodInfo);
        messageTypeRegistry.put(fullMethodName, messageTypes);

        logger.info("Registered gRPC method: {} -> {}", fullMethodName, intent);
    }

    /**
     * Gets gRPC method info by intent.
     *
     * @param intent the intent
     * @return the method info, or null if not found
     */
    public GrpcMethodInfo getMethodByIntent(String intent) {
        return methodRegistry.get(intent);
    }

    /**
     * Gets message types by full method name.
     *
     * @param fullMethodName the full method name (service/method)
     * @return the message type pair, or null if not found
     */
    public MessageTypePair getMessageTypes(String fullMethodName) {
        return messageTypeRegistry.get(fullMethodName);
    }

    /**
     * Registers custom message types for a method.
     *
     * @param fullMethodName the full method name
     * @param requestType    the request message type
     * @param responseType   the response message type
     */
    public void registerMessageTypes(String fullMethodName,
                                     Class<? extends Message> requestType,
                                     Class<? extends Message> responseType) {
        messageTypeRegistry.put(fullMethodName, new MessageTypePair(requestType, responseType));
    }

    /**
     * Infers message types from conductor method signature.
     *
     * @param conductorMethod the conductor method
     * @return the inferred message types
     */
    private MessageTypePair inferMessageTypes(ConductorMethod conductorMethod) {
        Method method = conductorMethod.getMethod();

        // Try to infer request type from parameters
        Class<? extends Message> requestType = null;
        if (conductorMethod.getParameters().size() == 1) {
            Class<?> paramType = conductorMethod.getParameters().get(0).getType();
            if (Message.class.isAssignableFrom(paramType)) {
                requestType = paramType.asSubclass(Message.class);
            }
        }

        // Try to infer response type from return type
        Class<? extends Message> responseType = null;
        Type returnType = method.getGenericReturnType();

        if (returnType instanceof Class<?> returnClass) {
            if (Message.class.isAssignableFrom(returnClass)) {
                responseType = returnClass.asSubclass(Message.class);
            }
        } else if (returnType instanceof ParameterizedType parameterizedType) {
            // Handle generic return types like CompletableFuture<Message>
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> genericClass) {
                if (Message.class.isAssignableFrom(genericClass)) {
                    responseType = genericClass.asSubclass(Message.class);
                }
            }
        }

        return new MessageTypePair(requestType, responseType);
    }

    /**
     * Information about a gRPC method.
     */
    public record GrpcMethodInfo(String intent, String serviceName, String methodName, String fullMethodName,
                                 MethodDescriptor.MethodType methodType, MessageTypePair messageTypes) {
    }

    /**
     * Pair of request and response message types.
     */
    public record MessageTypePair(Class<? extends Message> requestType, Class<? extends Message> responseType) {

        public boolean hasRequestType() {
            return requestType != null;
        }

        public boolean hasResponseType() {
            return responseType != null;
        }
    }
}
