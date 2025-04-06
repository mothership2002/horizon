package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ServerEngine<I extends RawInput, O extends RawOutput> {

    void run(AbstractHorizonContext<I, O> context) throws Exception;

    abstract class ServerEngineTemplate<I extends RawInput, O extends RawOutput> implements ServerEngine<I, O> {

        private static final Logger log = LoggerFactory.getLogger(ServerEngineTemplate.class);
        protected final AbstractHorizonContext<I, O> context;

        protected ServerEngineTemplate(AbstractHorizonContext<I, O> context) {
            this.context = context;
            log.info("ServerEngineTemplate initialized : {}", getClass().getSimpleName());
        }

        public void run(AbstractHorizonContext<I, O> context) throws Exception {
            doStart(context);
        }

        protected abstract void doStart(AbstractHorizonContext<I, O> context) throws Exception;
    }

}

