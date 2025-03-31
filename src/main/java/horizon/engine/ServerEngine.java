package horizon.engine;

import horizon.core.context.HorizonContext;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;


public interface ServerEngine<T extends RawInput, S extends RawOutput> {

    void run(HorizonContext<T, S> context, int port) throws Exception;


    abstract class ServerEngineTemplate<T extends RawInput, S extends RawOutput> implements ServerEngine<T, S> {

        public void run(HorizonContext<T, S> context, int port) throws Exception {
            doStart(context, port);
        }

        protected abstract void doStart(HorizonContext<T, S> context, int port) throws Exception;
    }

}

