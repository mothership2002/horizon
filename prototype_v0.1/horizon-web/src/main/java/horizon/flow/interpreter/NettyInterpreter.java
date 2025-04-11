package horizon.flow.interpreter;

import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.NormalizedInput;

public class NettyInterpreter extends AbstractProtocolInterpreter {

    /**
     * Constructs a new instance of NettyInterpreter.
     *
     * <p>This constructor explicitly calls the superclass constructor to ensure proper initialization.</p>
     */
    public NettyInterpreter() {
        super();
    }

    /**
     * Interprets the provided normalized input.
     *
     * <p>This stub implementation does not perform any interpretation and always returns {@code null}.
     *
     * @param normalizedInput the normalized input data to be interpreted
     * @return {@code null} as this method is a placeholder implementation
     */
    @Override
    public ParsedRequest interpret(NormalizedInput normalizedInput) {
        return null;
    }
}
