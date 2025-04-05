package horizon.data.stage;

import horizon.core.event.AbstractEventHorizon;
import horizon.core.stage.AbstractCentralStage;

public class DefaultCentralStage extends AbstractCentralStage {

    protected DefaultCentralStage(AbstractEventHorizon eventHorizon) {
        super(eventHorizon);
    }
}
