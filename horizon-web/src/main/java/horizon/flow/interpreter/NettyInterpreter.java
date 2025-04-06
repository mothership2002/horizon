package horizon.flow.interpreter;

import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.interpreter.ParsedRequest;
import horizon.core.flow.normalizer.NormalizedInput;

public class NettyInterpreter extends AbstractProtocolInterpreter {

    /**
     * Creates a new instance of NettyInterpreter.
     *
     * <p>This constructor calls the superclass constructor to ensure proper initialization
     * of inherited components. It provides an explicit entry point for future enhancements.</p>
     */
    public NettyInterpreter() {
        super();
    }

    /**
     * Interprets the given normalized input and returns a parsed request.
     *
     * <p>This implementation is currently a placeholder that always returns <code>null</code>.
     * Future enhancements may provide a proper parsing logic.</p>
     *
     * @param normalizedInput the normalized input to interpret
     * @return always <code>null</code>
     */
    @Override
    public ParsedRequest interpret(NormalizedInput normalizedInput) {
        return null;
    }
}
