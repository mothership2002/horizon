package horizon.parser.conductor;

import horizon.core.model.output.RawOutput;
import horizon.parser.pipeline.DefaultProtocolPipeline;
import horizon.protocol.http.input.HttpRawInput;

public class NettyConductor<T extends HttpRawInput> implements HttpConductor<T> {

    private final DefaultProtocolPipeline<T, ? extends RawOutput> pipeline;

    public NettyConductor(DefaultProtocolPipeline<T, ? extends RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
