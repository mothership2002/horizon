package horizon.core.conductor;

import horizon.core.conductor.gardian.Guardian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractConductorManager implements ConductorManager {

    private final Logger log = LoggerFactory.getLogger(AbstractConductorManager.class);
    protected final List<Guardian> guardians = new LinkedList<>();

    /**
     * Constructs a new AbstractConductorManager.
     * <p>
     * Logs an informational message indicating that the ConductorManager has been initialized,
     * including the simple name of the concrete class extending this abstract class.
     * </p>
     */
    public AbstractConductorManager() {
        log.info("ConductorManager initialized : {}", getClass().getSimpleName());
    }
}
