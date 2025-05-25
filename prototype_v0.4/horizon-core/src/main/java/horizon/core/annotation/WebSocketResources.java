package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container for multiple WebSocketResource annotations.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketResources {
    WebSocketResource[] value();
}
