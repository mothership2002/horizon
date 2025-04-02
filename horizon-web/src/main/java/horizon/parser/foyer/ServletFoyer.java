package horizon.parser.foyer;

import horizon.core.model.output.RawOutput;
import horizon.parser.pipeline.DefaultProtocolPipeline;
import horizon.protocol.http.input.HttpRawInput;

public class ServletFoyer<T extends HttpRawInput> implements HttpFoyer<T> {

    private final DefaultProtocolPipeline<T, RawOutput> pipeline;

    public ServletFoyer(DefaultProtocolPipeline<T, RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
