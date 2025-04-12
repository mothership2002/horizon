package horizon.core.stage;

import horizon.core.event.AbstractEventHorizon;

public abstract class AbstractShadowStage implements ShadowStage {

    protected final AbstractEventHorizon eventHorizon;

    public AbstractShadowStage(AbstractEventHorizon eventHorizon) {
        this.eventHorizon = eventHorizon;
    }
}
