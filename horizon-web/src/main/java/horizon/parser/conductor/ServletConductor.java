package horizon.parser.conductor;

import horizon.core.model.output.RawOutput;
import horizon.parser.pipeline.DefaultProtocolPipeline;
import horizon.protocol.http.input.HttpRawInput;

public class ServletConductor<T extends HttpRawInput> implements HttpConductor<T> {

    private final DefaultProtocolPipeline<T, RawOutput> pipeline;

    public ServletConductor(DefaultProtocolPipeline<T, RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
