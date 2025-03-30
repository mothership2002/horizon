package horizon.core.context;

import horizon.core.input.RawInput;
import horizon.engine.ServerEngine;
import horizon.engine.ServerEngineTemplate;

import java.util.ArrayList;
import java.util.List;

public class HorizonContextCoordinator {

    private final List<HorizonContext<? extends RawInput>> contexts = new ArrayList<>();

    public void register(HorizonContext<? extends RawInput> context) {
        contexts.add(context);
    }

    public void runAll(int port) throws Exception {
        for (HorizonContext<? extends RawInput> context : contexts) {
            runContext(context, port);
        }
    }

    private <T extends RawInput> void runContext(HorizonContext<T> context, int port) throws Exception {
        ServerEngineTemplate<T> engine = context.provideEngine();
        engine.run(context, port);
    }
}
