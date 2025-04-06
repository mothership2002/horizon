package horizon.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventHorizon implements EventHorizon {

    private final Logger log = LoggerFactory.getLogger(AbstractEventHorizon.class);

    /**
     * Constructs a new AbstractEventHorizon instance and logs its initialization.
     *
     * <p>The constructor logs an informational message with the simple name of the concrete subclass,
     * indicating that the EventHorizon is being initialized.</p>
     */
    public AbstractEventHorizon() {
        log.info("Initializing EventHorizon : {}", getClass().getSimpleName());
    }
}
