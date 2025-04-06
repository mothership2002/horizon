package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ServerEngine<I extends RawInput, O extends RawOutput> {

    /**
 * Executes the server engine using the specified context.
 *
 * @param context the execution context containing the raw input and output data
 * @throws Exception if an error occurs during the engine's execution
 */
void run(AbstractHorizonContext<I, O> context) throws Exception;

    abstract class ServerEngineTemplate<I extends RawInput, O extends RawOutput> implements ServerEngine<I, O> {

        private static final Logger log = LoggerFactory.getLogger(ServerEngineTemplate.class);
        protected final AbstractHorizonContext<I, O> context;

        /**
         * Constructs a new ServerEngineTemplate using the specified context.
         *
         * @param context the server engine context that provides the raw input and output data
         */
        protected ServerEngineTemplate(AbstractHorizonContext<I, O> context) {
            this.context = context;
            log.info("ServerEngineTemplate initialized : {}", getClass().getSimpleName());
        }

        /**
         * Executes the server engine using the specified context.
         *
         * <p>This method delegates to the {@code doStart} method to perform the engine-specific startup logic.</p>
         *
         * @param context the server engine context containing raw input and output for execution
         * @throws Exception if an error occurs during engine startup
         */
        public void run(AbstractHorizonContext<I, O> context) throws Exception {
            doStart(context);
        }

        /**
 * Initiates the server engine startup process using the provided context.
 *
 * <p>Subclasses must implement this method to perform any necessary initialization and startup 
 * logic for processing raw input and output encapsulated within the context.</p>
 *
 * @param context the server context providing access to raw input and output interfaces
 * @throws Exception if an error occurs during the startup process
 */
protected abstract void doStart(AbstractHorizonContext<I, O> context) throws Exception;
    }

}

