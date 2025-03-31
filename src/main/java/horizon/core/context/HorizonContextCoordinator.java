package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.engine.ServerEngine;

import java.util.ArrayList;
import java.util.List;

public class HorizonContextCoordinator {

    private final List<HorizonContext<? extends RawInput, ? extends RawOutput>> contexts = new ArrayList<>();

    public void register(HorizonContext<? extends RawInput, ? extends RawOutput> context) {
        contexts.add(context);
    }

    public void runAll(int port) throws Exception {
        for (HorizonContext<? extends RawInput, ? extends RawOutput> context : contexts) {
            runContext(context, port);
        }
    }

    private <T extends RawInput, S extends RawOutput> void runContext(HorizonContext<T, S> context, int port) throws Exception {
        ServerEngine.ServerEngineTemplate<T, S> engine = context.provideEngine();
        engine.run(context, port);
    }
}
