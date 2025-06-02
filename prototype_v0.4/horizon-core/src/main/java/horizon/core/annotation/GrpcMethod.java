package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * @deprecated gRPC support has been removed from Horizon Framework.
 * This annotation is no longer used and will be removed in the next version.
 * 
 * @since 0.4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated(since = "0.4", forRemoval = true)
public @interface GrpcMethod {
    
    Class<?> requestType() default Object.class;
    Class<?> responseType() default Object.class;
    StreamingType streaming() default StreamingType.UNARY;
    boolean compressed() default false;
    long deadlineMs() default 0;
    
    enum StreamingType {
        UNARY,
        SERVER_STREAMING,
        CLIENT_STREAMING,
        BIDI_STREAMING
    }
}
