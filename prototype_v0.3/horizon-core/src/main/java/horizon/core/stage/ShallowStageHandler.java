package horizon.core.stage;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawOutput;

public interface ShallowStageHandler {
    RawOutput handleFailure(Throwable cause, HorizonContext context);
}
