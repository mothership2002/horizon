package horizon.core.annotation;

import java.lang.annotation.*;

/**
 * Defines a custom protocol that can be used in the application.
 * This allows extending beyond the built-in protocols.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomProtocol {
    /**
     * The unique name of the protocol.
     */
    String name();
    
    /**
     * Display name of the protocol.
     */
    String displayName() default "";
    
    /**
     * Description of the protocol.
     */
    String description() default "";
    
    /**
     * The adapter class that handles this protocol.
     */
    Class<? extends horizon.core.protocol.ProtocolAdapter> adapterClass();
}
