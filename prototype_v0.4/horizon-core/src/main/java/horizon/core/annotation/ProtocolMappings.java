package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Container annotation for multiple ProtocolMapping annotations.
 * 
 * @deprecated Use @ProtocolAccess with multiple schemas instead
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface ProtocolMappings {
    ProtocolMapping[] value();
}
