package horizon.core.stage;

import horizon.core.event.AbstractEventHorizon;

public abstract class AbstractCentralStage implements CentralStage {

    protected final AbstractEventHorizon eventHorizon;

    protected AbstractCentralStage(AbstractEventHorizon eventHorizon) {
        this.eventHorizon = eventHorizon;
    }
}
