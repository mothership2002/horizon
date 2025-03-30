package horizon.engine;

import horizon.core.context.HorizonContext;
import horizon.core.input.RawInput;

public interface ServerEngine<T extends RawInput> {

    void run(HorizonContext<T> context, int port) throws Exception;
}