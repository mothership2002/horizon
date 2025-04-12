package horizon.core.annotation;

import horizon.core.constant.Scheme;
import horizon.core.model.input.RawInput;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sentinel {

    Scheme[] scheme();

    int order();

    SentinelDirection direction();

    enum SentinelDirection {
        INBOUND,
        OUTBOUND,
        BOTH
    }
}
