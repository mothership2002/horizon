package horizon.engine.servlet.parser.conductor;

import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.conductor.ProtocolConductor;

public class ServletConductor<T extends HttpRawInput> implements ProtocolConductor.HttpConductor<T> {

    private final ProtocolPipeline<T, RawOutput> pipeline;

    public ServletConductor(ProtocolPipeline<T, RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
