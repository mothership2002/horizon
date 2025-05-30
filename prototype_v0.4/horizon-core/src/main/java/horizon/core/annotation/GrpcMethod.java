package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Defines gRPC-specific method mapping for a conductor method.
 * This annotation is used in conjunction with @ProtocolAccess to provide
 * more detailed gRPC configuration.
 * 
 * Example:
 * <pre>
 * @Intent("create")
 * @ProtocolAccess(
 *     schema = @ProtocolSchema(protocol = "gRPC", value = "UserService/CreateUser")
 * )
 * @GrpcMethod(
 *     requestType = CreateUserRequest.class,
 *     responseType = CreateUserResponse.class,
 *     streaming = GrpcMethod.StreamingType.UNARY
 * )
 * public CreateUserResponse createUser(CreateUserRequest request) {
 *     // Implementation
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GrpcMethod {
    
    /**
     * The request message type.
     * If not specified, will be inferred from method parameters.
     */
    Class<?> requestType() default Object.class;
    
    /**
     * The response message type.
     * If not specified, will be inferred from method return type.
     */
    Class<?> responseType() default Object.class;
    
    /**
     * The streaming type of the method.
     */
    StreamingType streaming() default StreamingType.UNARY;
    
    /**
     * Whether to use compression for this method.
     */
    boolean compressed() default false;
    
    /**
     * Maximum deadline in milliseconds (0 = no deadline).
     */
    long deadlineMs() default 0;
    
    /**
     * gRPC streaming types.
     */
    enum StreamingType {
        /**
         * Simple RPC: single request, single response.
         */
        UNARY,
        
        /**
         * Server streaming: single request, stream of responses.
         */
        SERVER_STREAMING,
        
        /**
         * Client streaming: stream of requests, single response.
         */
        CLIENT_STREAMING,
        
        /**
         * Bidirectional streaming: stream of requests, stream of responses.
         */
        BIDI_STREAMING
    }
}
