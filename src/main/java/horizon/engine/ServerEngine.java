package horizon.engine;

import horizon.core.context.HorizonContext;
import horizon.core.input.RawInput;


public interface ServerEngine<T extends RawInput> {

    void run(HorizonContext<T> context, int port) throws Exception;


    abstract class ServerEngineTemplate<T extends RawInput> implements ServerEngine<T> {

        public void run(HorizonContext<T> context, int port) throws Exception {
            doStart(context, port);
        }

        protected abstract void doStart(HorizonContext<T> context, int port) throws Exception;
    }

}

