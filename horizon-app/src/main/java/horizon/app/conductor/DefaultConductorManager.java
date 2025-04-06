package horizon.app.conductor;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.model.output.RawOutput;

public class DefaultConductorManager extends AbstractConductorManager {



    /**
     * Processes the specified parsed request by returning a RawOutput instance.
     *
     * <p>This implementation creates an anonymous RawOutput subclass that overrides its
     * {@code hashCode} method to delegate to the superclass's implementation.</p>
     *
     * @param request the parsed request to be processed
     * @return a RawOutput instance representing the conduction result
     */
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
