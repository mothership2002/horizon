package horizon.app.context;

import horizon.app.conductor.DefaultConductorManager;
import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;

public class DefaultPresentationContext extends AbstractHorizonContext.AbstractPresentationContext {

    /**
     * Returns a new instance of the default conductor manager.
     *
     * <p>This method instantiates a {@link DefaultConductorManager} to manage conductor operations within the Horizon presentation context.
     *
     * @return a new instance of {@link DefaultConductorManager}
     */
    @Override
    public AbstractConductorManager provideConductorManager() {
        return new DefaultConductorManager();
    }
}
