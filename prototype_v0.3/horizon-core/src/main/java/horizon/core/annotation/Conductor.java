package horizon.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a class as a Conductor in the Horizon framework.
 * A Conductor combines the responsibilities of a Controller and a Service:
 * - It handles incoming requests (like a Controller)
 * - It contains business logic (like a Service)
 * - It resolves payloads into commands that can be executed
 * 
 * Classes annotated with @Conductor should implement the Conductor interface
 * or extend a base class that implements it.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Conductor {

    /**
     * The name of the conductor.
     * If not specified, the class name will be used.
     * 
     * @return the conductor name
     */
    String value() default "";

    /**
     * The namespace for all intents handled by this conductor.
     * This namespace will be prepended to all intent names specified in @Intent annotations
     * on methods within this class.
     * 
     * @return the namespace
     */
    String namespace() default "";

    /**
     * Whether this conductor is transactional.
     * If true, all methods will be executed within a transaction.
     * 
     * @return true if transactional, false otherwise
     */
    boolean transactional() default false;

    /**
     * Whether this conductor is validated.
     * If true, method parameters will be validated using the validation framework.
     * 
     * @return true if validated, false otherwise
     */
    boolean validated() default true;
}
