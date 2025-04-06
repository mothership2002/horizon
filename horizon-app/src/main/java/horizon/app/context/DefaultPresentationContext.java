package horizon.app.context;

import horizon.app.conductor.DefaultConductorManager;
import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;

public class DefaultPresentationContext extends AbstractHorizonContext.AbstractPresentationContext {

    /**
     * Returns a new default conductor manager.
     *
     * <p>This implementation creates and returns a concrete instance of {@link DefaultConductorManager},
     * enabling the conductor-related functionality within the context.
     *
     * @return a new instance of {@link DefaultConductorManager}
     */
    @Override
    public AbstractConductorManager provideConductorManager() {
        return new DefaultConductorManager();
    }
}
