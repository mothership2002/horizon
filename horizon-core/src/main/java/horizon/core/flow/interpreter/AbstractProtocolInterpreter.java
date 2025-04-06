package horizon.core.flow.interpreter;

import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProtocolInterpreter implements ProtocolInterpreter {

    private final Logger log = LoggerFactory.getLogger(AbstractProtocolInterpreter.class);

    /**
     * Constructs a new AbstractProtocolInterpreter instance and logs its initialization.
     *
     * <p>This constructor logs an informational message that includes the simple name of the concrete class
     * extending AbstractProtocolInterpreter, aiding in debugging and tracking the instantiation process.
     */
    public AbstractProtocolInterpreter() {
        log.info("Initializing ProtocolInterpreter : {}", getClass().getSimpleName());
    }
}
