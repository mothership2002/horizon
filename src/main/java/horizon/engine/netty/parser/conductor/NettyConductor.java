package horizon.engine.netty.parser.conductor;

import horizon.core.input.http.HttpRawInput;
import horizon.core.output.RawOutput;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.conductor.ProtocolConductor;

public class NettyConductor<T extends HttpRawInput> implements ProtocolConductor.HttpConductor<T> {

    private final ProtocolPipeline<T> pipeline;

    public NettyConductor(ProtocolPipeline<T> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
