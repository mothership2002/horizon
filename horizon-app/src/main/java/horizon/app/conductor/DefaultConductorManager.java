package horizon.app.conductor;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.model.output.RawOutput;

public class DefaultConductorManager extends AbstractConductorManager {



    /**
     * Processes the parsed request by returning a RawOutput instance.
     *
     * <p>This method returns an anonymous subclass of RawOutput with an overridden 
     * {@code hashCode} method that delegates to the superclass implementation. It encapsulates 
     * the output generated from processing the provided request.</p>
     *
     * @param request the parsed request to process
     * @return a RawOutput instance with customized {@code hashCode} behavior
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
