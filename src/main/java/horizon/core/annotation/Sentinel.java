package horizon.core.annotation;

import horizon.core.input.RawInput;
import horizon.core.parser.pipeline.ProtocolPipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sentinel {

    RawInput.Scheme[] scheme();

    int order();

    SentinelDirection direction();

    enum SentinelDirection {
        INBOUND,
        OUTBOUND,
        BOTH
    }
}
