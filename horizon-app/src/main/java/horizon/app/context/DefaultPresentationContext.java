package horizon.app.context;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;

public class DefaultPresentationContext extends AbstractHorizonContext.AbstractPresentationContext {

    @Override
    public AbstractConductorManager provideConductorManager() {
        return null;
    }
}
