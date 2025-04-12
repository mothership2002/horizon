package horizon.core.annotation;

import horizon.core.constant.Scheme;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Rendezvous {
    Scheme scheme();
}
