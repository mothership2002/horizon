package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;


public interface ServerEngine<T extends RawInput, S extends RawOutput> {

    void run(AbstractHorizonContext<T, S> context) throws Exception;


    abstract class ServerEngineTemplate<T extends RawInput, S extends RawOutput> implements ServerEngine<T, S> {

        public void run(AbstractHorizonContext<T, S> context) throws Exception {
            doStart(context);
        }

        protected abstract void doStart(AbstractHorizonContext<T, S> context) throws Exception;
    }

}

