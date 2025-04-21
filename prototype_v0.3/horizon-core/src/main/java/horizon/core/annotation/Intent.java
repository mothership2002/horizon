package horizon.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping requests onto methods in request-handling classes.
 * This annotation is used to map requests to conductor methods based on the intent name.
 * 
 * This annotation can be applied at the class level to create a base mapping for
 * all methods within the class, or at the method level to map specific methods to
 * specific intents.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Intent {

    /**
     * The primary intent name expressed by this annotation.
     * This is a unique identifier for the intent that this method handles.
     * 
     * @return the intent name
     */
    String value() default "";

    /**
     * Alias for {@link #value()}.
     * 
     * @return the intent name
     */
    String name() default "";

    /**
     * Additional metadata for the intent.
     * This can be used to provide extra information about the intent.
     * 
     * @return the metadata
     */
    String[] metadata() default {};
}
