package horizon.core.model;

import horizon.core.model.output.RawOutput;

public interface RawOutputBuilder<S extends RawOutput> {

    S build(Object result);
}