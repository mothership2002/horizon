package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.ArrayList;
import java.util.List;

public class HorizonContextCoordinator {

    private final List<AbstractHorizonContext<? extends RawInput, ? extends RawOutput>> contexts = new ArrayList<>();

    public void register(AbstractHorizonContext<? extends RawInput, ? extends RawOutput> context) {
        contexts.add(context);
    }

    public void runAll() throws Exception {
        for (AbstractHorizonContext<? extends RawInput, ? extends RawOutput> context : contexts) {
            runContext(context);
        }
    }

    private <T extends RawInput, S extends RawOutput> void runContext(AbstractHorizonContext<T, S> context) throws Exception {
        ServerEngine.ServerEngineTemplate<T, S> engine = context.provideEngine();
        engine.run(context);
    }
}
