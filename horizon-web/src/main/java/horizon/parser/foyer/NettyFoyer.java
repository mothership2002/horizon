package horizon.parser.foyer;

import horizon.core.flow.parser.pipeline.ProtocolPipeline;
import horizon.core.model.output.RawOutput;
import horizon.protocol.http.input.HttpRawInput;

public class NettyFoyer<T extends HttpRawInput> implements HttpFoyer<T> {

    private final ProtocolPipeline<T, ? extends RawOutput> pipeline;

    public NettyFoyer(ProtocolPipeline<T, ? extends RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
