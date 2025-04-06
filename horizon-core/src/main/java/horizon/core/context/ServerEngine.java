package horizon.core.context;

import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ServerEngine<I extends RawInput, O extends RawOutput> {

    /**
 * Executes the server engine using the provided context.
 * 
 * <p>This method initiates the server engine's operation by leveraging the specified context,
 * which encapsulates the input and output configurations. Implementations should use this context
 * to perform any necessary startup or initialization tasks.</p>
 * 
 * @param context the server context containing configuration and state required for engine startup
 * @throws Exception if an error occurs during the engine's execution
 */
void run(AbstractHorizonContext<I, O> context) throws Exception;

    abstract class ServerEngineTemplate<I extends RawInput, O extends RawOutput> implements ServerEngine<I, O> {

        private static final Logger log = LoggerFactory.getLogger(ServerEngineTemplate.class);
        protected final AbstractHorizonContext<I, O> context;

        /**
         * Constructs a new ServerEngineTemplate with the specified execution context.
         *
         * <p>This constructor initializes the server engine template with the context that provides the necessary
         * configuration and I/O resources for the engine's operation.
         *
         * @param context the execution context for the server engine
         */
        protected ServerEngineTemplate(AbstractHorizonContext<I, O> context) {
            this.context = context;
            log.info("ServerEngineTemplate initialized : {}", getClass().getSimpleName());
        }

        /**
         * Executes the server engine using the provided execution context.
         *
         * <p>This method delegates the startup process to the abstract {@code doStart} method,
         * which must be implemented by subclasses to define the engine's specific startup behavior.</p>
         *
         * @param context the execution context containing the necessary input and output configuration
         *                for running the server engine
         * @throws Exception if an error occurs during the engine startup process
         */
        public void run(AbstractHorizonContext<I, O> context) throws Exception {
            doStart(context);
        }

        /**
 * Executes the startup logic for the server engine using the provided context.
 *
 * <p>This method must be implemented by subclasses to perform any necessary initialization or execution
 * procedures required to start the server engine. It is called by the {@code run} method of the engine template,
 * supplying the essential context for runtime operations.
 *
 * @param context the engine context providing input, output, and configuration data
 * @throws Exception if an error occurs during the startup process
 */
protected abstract void doStart(AbstractHorizonContext<I, O> context) throws Exception;
    }

}

