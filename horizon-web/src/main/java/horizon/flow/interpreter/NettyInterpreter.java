package horizon.flow.interpreter;

import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.NormalizedInput;

public class NettyInterpreter extends AbstractProtocolInterpreter {

    /**
     * Constructs a new NettyInterpreter.
     *
     * <p>This constructor explicitly calls the superclass constructor to ensure the proper initialization
     * of the inherited AbstractProtocolInterpreter components.</p>
     */
    public NettyInterpreter() {
        super();
    }

    /**
     * Interprets the provided normalized input and converts it into a parsed request.
     *
     * <p>This implementation currently returns {@code null} and serves as a placeholder
     * for future interpretation logic.</p>
     *
     * @param normalizedInput the normalized input to process
     * @return {@code null} as no interpretation is performed
     */
    @Override
    public ParsedRequest interpret(NormalizedInput normalizedInput) {
        return null;
    }
}
