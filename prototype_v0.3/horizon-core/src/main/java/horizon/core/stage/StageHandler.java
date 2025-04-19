package horizon.core.stage;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawOutput;

public interface StageHandler {
    RawOutput handle(HorizonContext context);
}

