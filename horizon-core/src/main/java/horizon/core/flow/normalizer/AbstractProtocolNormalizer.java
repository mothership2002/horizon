package horizon.core.flow.normalizer;

import horizon.core.model.input.RawInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolNormalizer<I extends RawInput> implements ProtocolNormalizer<I> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolNormalizer.class.getName());

    /**
     * Constructs a new instance of AbstractProtocolNormalizer and logs its creation.
     *
     * <p>This constructor logs an informational message that includes the simple name
     * of the concrete class, indicating that a protocol normalizer has been instantiated.</p>
     */
    public AbstractProtocolNormalizer() {
        log.info("ProtocolNormalizer constructed : {}", getClass().getSimpleName());
    }
}
