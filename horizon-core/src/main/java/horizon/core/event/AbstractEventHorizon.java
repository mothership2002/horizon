package horizon.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventHorizon implements EventHorizon {

    private final Logger log = LoggerFactory.getLogger(AbstractEventHorizon.class);

    /**
     * Constructs an instance of AbstractEventHorizon and logs an informational message that includes
     * the simple name of the concrete subclass being initialized.
     */
    public AbstractEventHorizon() {
        log.info("Initializing EventHorizon : {}", getClass().getSimpleName());
    }
}
