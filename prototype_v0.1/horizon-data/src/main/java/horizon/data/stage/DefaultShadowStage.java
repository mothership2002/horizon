package horizon.data.stage;

import horizon.core.event.AbstractEventHorizon;
import horizon.core.stage.AbstractShadowStage;

public class DefaultShadowStage extends AbstractShadowStage {

    public DefaultShadowStage(AbstractEventHorizon eventHorizon) {
        super(eventHorizon);
    }
}
