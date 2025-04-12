package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ServerEngine<I extends RawInput, O extends RawOutput> {

    /**
 * Executes the server engine using the specified context.
 *
 * @param context the execution context encapsulating raw input and output data
 * @throws Exception if an error occurs during the engine's execution
 */
void run(AbstractHorizonContext<I, O> context) throws Exception;

    abstract class ServerEngineTemplate<I extends RawInput, O extends RawOutput> implements ServerEngine<I, O> {

        private static final Logger log = LoggerFactory.getLogger(ServerEngineTemplate.class);
        protected final AbstractHorizonContext<I, O> context;

        /**
         * Constructs a ServerEngineTemplate with the specified execution context.
         * <p>
         * The provided context encapsulates input and output required for processing,
         * and its initialization is logged with the template's class name.
         * </p>
         *
         * @param context the execution context for the server engine
         */
        protected ServerEngineTemplate(AbstractHorizonContext<I, O> context) {
            this.context = context;
            log.info("ServerEngineTemplate initialized : {}", getClass().getSimpleName());
        }

        /**
         * Executes the server engine by processing the given context.
         *
         * <p>This method delegates the engine's startup logic to the abstract {@code doStart} method,
         * which must be implemented by subclasses to define specific processing behavior.
         *
         * @param context the context encapsulating input and output data for the engine
         * @throws Exception if an error occurs during processing
         */
        public void run(AbstractHorizonContext<I, O> context) throws Exception {
            doStart(context);
        }

        /**
 * Executes the startup routine for the server engine using the provided context.
 *
 * <p>Subclasses must implement this method to initiate engine processing by utilizing the context,
 * which encapsulates the raw input and output required for operation.</p>
 *
 * @param context the engine context containing raw input and output channels
 * @throws Exception if an error occurs during the startup process
 */
protected abstract void doStart(AbstractHorizonContext<I, O> context) throws Exception;
    }

}

