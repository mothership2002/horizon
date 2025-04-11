package horizon.core.conductor;

import horizon.core.conductor.gardian.Guardian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class AbstractConductorManager implements ConductorManager {

    private final Logger log = LoggerFactory.getLogger(AbstractConductorManager.class);
    protected final List<Guardian> guardians = new LinkedList<>();
    protected final ExecutorService conductorExecutor;

    /**
     * Constructs an AbstractConductorManager and logs its initialization.
     *
     * <p>This constructor logs an informational message with the simple name of the class, indicating that the
     * ConductorManager instance has been initialized.</p>
     */
    public AbstractConductorManager(ExecutorService conductorExecutor) {
        this.conductorExecutor = conductorExecutor;
        log.info("ConductorManager initialized : {}", getClass().getSimpleName());
    }
}
