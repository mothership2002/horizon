package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container for multiple WebSocketResource annotations.
 * 
 * @deprecated Use @ProtocolAccess with multiple schemas instead
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface WebSocketResources {
    WebSocketResource[] value();
}
