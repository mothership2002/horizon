package horizon.core.flow.normalizer;

import horizon.core.model.input.RawInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolNormalizer<I extends RawInput> implements ProtocolNormalizer<I> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProtocolNormalizer.class.getName());

    public AbstractProtocolNormalizer() {
        log.info("ProtocolNormalizer constructed : {}", getClass().getSimpleName());
    }
}
