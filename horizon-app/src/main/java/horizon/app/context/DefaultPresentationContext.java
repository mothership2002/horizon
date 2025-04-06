package horizon.app.context;

import horizon.app.conductor.DefaultConductorManager;
import horizon.core.conductor.AbstractConductorManager;
import horizon.core.context.AbstractHorizonContext;

import java.util.concurrent.ExecutorService;

public class DefaultPresentationContext extends AbstractHorizonContext.AbstractPresentationContext {


    public DefaultPresentationContext(ExecutorService conductorExecutor) {
        super(conductorExecutor);
    }

    @Override
    public AbstractConductorManager provideConductorManager() {
        return new DefaultConductorManager(conductorExecutor);
    }
}
