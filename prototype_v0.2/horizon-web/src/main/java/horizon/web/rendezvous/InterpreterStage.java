package horizon.web.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.interpreter.AbstractInterpreter;

public interface InterpreterStage<I extends RawInput, O extends RawOutput> {
    BuildStage<I, O> withInterpreter(AbstractInterpreter interpreter);
}
