package horizon.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventHorizon implements EventHorizon {

    private final Logger log = LoggerFactory.getLogger(AbstractEventHorizon.class);

    public AbstractEventHorizon() {
        log.info("Initializing EventHorizon : {}", getClass().getSimpleName());
    }
}
