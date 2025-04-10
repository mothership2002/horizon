package horizon.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventHorizon implements EventHorizon {

    private final Logger log = LoggerFactory.getLogger(AbstractEventHorizon.class);

    /**
     * Constructs an {@code AbstractEventHorizon} instance and logs its initialization.
     * <p>
     * The constructor logs an informational message that includes the simple name of the
     * concrete class extending {@code AbstractEventHorizon}, providing runtime traceability
     * of the event horizon initialization.
     * </p>
     */
    public AbstractEventHorizon() {
        log.info("Initializing EventHorizon : {}", getClass().getSimpleName());
    }
}
