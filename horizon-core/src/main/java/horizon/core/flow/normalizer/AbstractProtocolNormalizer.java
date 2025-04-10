package horizon.core.flow.normalizer;

import horizon.core.model.input.RawInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolNormalizer<I extends RawInput> implements ProtocolNormalizer<I> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolNormalizer.class.getName());

    /**
     * Constructs an instance of the protocol normalizer and logs its creation.
     *
     * <p>This constructor initializes the normalizer and logs an informational message, including the
     * simple name of the concrete subclass, to aid in tracking instance creation.
     */
    public AbstractProtocolNormalizer() {
        log.info("ProtocolNormalizer constructed : {}", getClass().getSimpleName());
    }
}
