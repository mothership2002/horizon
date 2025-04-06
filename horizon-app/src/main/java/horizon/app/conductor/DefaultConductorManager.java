package horizon.app.conductor;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.model.output.RawOutput;

public class DefaultConductorManager extends AbstractConductorManager {



    /**
     * Processes the given parsed request and returns a raw output instance.
     *
     * <p>This implementation creates and returns an instance of an anonymous subclass of
     * RawOutput. The subclass overrides the {@code hashCode} method to call the superclass's
     * implementation, ensuring that a valid output object is produced.
     *
     * @param request the parsed request containing input data for processing
     * @return a new RawOutput instance representing the conduction result
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
