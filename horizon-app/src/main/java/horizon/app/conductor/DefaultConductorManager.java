package horizon.app.conductor;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.ExecutorService;

public class DefaultConductorManager extends AbstractConductorManager {

    public DefaultConductorManager(ExecutorService conductorExecutor) {
        super(conductorExecutor);
    }

    @Override
    public Object conduct(ParsedRequest request) {
        return new RawOutput() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }
        };
    }
}
