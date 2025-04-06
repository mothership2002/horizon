package horizon.app.context;

import horizon.app.conductor.DefaultConductorManager;
import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;

public class DefaultPresentationContext extends AbstractHorizonContext.AbstractPresentationContext {

    /**
     * Returns the conductor manager for this presentation context.
     *
     * <p>This implementation returns a new instance of DefaultConductorManager, which manages
     * conductor-related operations within the Horizon application.</p>
     *
     * @return a DefaultConductorManager instance
     */
    @Override
    public AbstractConductorManager provideConductorManager() {
        return new DefaultConductorManager();
    }
}
