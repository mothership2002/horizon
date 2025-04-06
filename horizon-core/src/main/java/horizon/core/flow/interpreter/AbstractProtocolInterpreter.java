package horizon.core.flow.interpreter;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolInterpreter implements ProtocolInterpreter {

    private final Logger log = LoggerFactory.getLogger(AbstractProtocolInterpreter.class);

    /**
     * Creates an AbstractProtocolInterpreter instance and logs an informational message
     * indicating the initialization of the ProtocolInterpreter with the simple name
     * of the concrete subclass.
     */
    public AbstractProtocolInterpreter() {
        log.info("Initializing ProtocolInterpreter : {}", getClass().getSimpleName());
    }
}
