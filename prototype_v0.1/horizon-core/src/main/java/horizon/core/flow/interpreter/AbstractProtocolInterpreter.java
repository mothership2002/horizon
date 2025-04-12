package horizon.core.flow.interpreter;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolInterpreter implements ProtocolInterpreter {

    private final Logger log = LoggerFactory.getLogger(AbstractProtocolInterpreter.class);

    /**
     * Initializes an instance of the AbstractProtocolInterpreter.
     *
     * <p>Logs an informational message indicating that the ProtocolInterpreter is being initialized,
     * including the simple name of the concrete subclass.
     */
    public AbstractProtocolInterpreter() {
        log.info("Initializing ProtocolInterpreter : {}", getClass().getSimpleName());
    }
}
