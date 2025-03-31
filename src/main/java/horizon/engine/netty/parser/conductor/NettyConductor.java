package horizon.engine.netty.parser.conductor;

import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.parser.pipeline.ProtocolPipeline;
import horizon.core.parser.conductor.ProtocolConductor;

public class NettyConductor<T extends HttpRawInput> implements ProtocolConductor.HttpConductor<T> {

    private final ProtocolPipeline<T, ? extends RawOutput> pipeline;

    public NettyConductor(ProtocolPipeline<T, ? extends RawOutput> pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public RawOutput process(T rawInput) {
        return pipeline.handle(rawInput);
    }
}
