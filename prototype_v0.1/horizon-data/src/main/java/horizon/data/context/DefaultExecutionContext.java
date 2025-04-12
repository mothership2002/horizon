package horizon.data.context;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.event.AbstractEventHorizon;
import horizon.core.stage.AbstractCentralStage;
import horizon.core.stage.AbstractShadowStage;

import java.util.concurrent.ExecutorService;

public class DefaultExecutionContext extends AbstractHorizonContext.AbstractExecutionContext {

    public DefaultExecutionContext(ExecutorService eventExecutor) {
        super(eventExecutor);
    }

    @Override
    public AbstractEventHorizon provideEventHorizon() {
        return null;
    }

    @Override
    public AbstractShadowStage provideShadowStage() {
        return null;
    }

    @Override
    public AbstractCentralStage provideCentralStage() {
        return null;
    }
}
