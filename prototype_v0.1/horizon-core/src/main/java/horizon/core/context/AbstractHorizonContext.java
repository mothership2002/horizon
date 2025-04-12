package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.util.SentinelScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

public abstract class AbstractHorizonContext<I extends RawInput, O extends RawOutput> implements HorizonContext<I, O> {

    private final static Logger log = LoggerFactory.getLogger(AbstractHorizonContext.class);

    protected final AbstractProtocolContext<I, O> protocolContext;
    protected final AbstractPresentationContext presentationContext;
    protected final AbstractExecutionContext executionContext;
    protected final Properties properties;
    protected final Scheme scheme;
    protected final SentinelScanner sentinelScanner;

    /**
     * Constructs a new AbstractHorizonContext with the specified context components and configuration.
     *
     * @param protocolContext the protocol context responsible for handling input and output operations
     * @param presentationContext the presentation context managing presentation-specific logic
     * @param executionContext the execution context responsible for execution-related tasks
     * @param properties the configuration properties for the context
     * @param scheme the scheme defining the protocol structure of the context
     * @param sentinelScanner the scanner used for security or validation within the context
     */
    public AbstractHorizonContext(AbstractProtocolContext<I, O> protocolContext, AbstractPresentationContext presentationContext,
                                  AbstractExecutionContext executionContext, Properties properties, Scheme scheme, SentinelScanner sentinelScanner) {
        this.protocolContext = protocolContext;
        this.presentationContext = presentationContext;
        this.executionContext = executionContext;
        this.properties = properties;
        this.scheme = scheme;
        this.sentinelScanner = sentinelScanner;
        log.info("Initializing HorizonContext : {}", getClass().getSimpleName());
    }

    public abstract static class AbstractProtocolContext<I extends RawInput, O extends RawOutput> implements ProtocolContext<I, O> {

        protected final RawOutputBuilder<O> rawOutputBuilder;
        protected final AbstractProtocolFoyer<I> foyer;

        /**
         * Constructs a new protocol context with the specified raw output builder and protocol foyer.
         *
         * <p>This constructor initializes the context by setting its raw output builder and protocol
         * foyer, then logs the initialization event.</p>
         *
         * @param rawOutputBuilder the builder used for generating raw output
         * @param foyer the protocol foyer responsible for protocol-specific initialization
         */
        public AbstractProtocolContext(RawOutputBuilder<O> rawOutputBuilder,
                                       AbstractProtocolFoyer<I> foyer) {

            this.rawOutputBuilder = rawOutputBuilder;
            this.foyer = foyer;
            log.info("Initializing ProtocolContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractPresentationContext implements PresentationContext {
        protected final ExecutorService conductorExecutor;

        /**
         * Constructs a new AbstractPresentationContext.
         *
         * <p>This constructor logs an info-level message indicating that the presentation context is being initialized, using the class's simple name for identification.</p>
         */
        public AbstractPresentationContext(ExecutorService conductorExecutor) {
            this.conductorExecutor = conductorExecutor;
            log.info("Initializing PresentationContext : {}", getClass().getSimpleName());
        }
    }
    /**
     * Constructs a new execution context and logs its initialization.
     *
     * <p>This constructor instantiates the execution context component and emits an
     * informational log message containing the simple name of the class.
     */
    public abstract static class AbstractExecutionContext implements ExecutionContext {
        protected final ExecutorService centinelExecutor;

        public AbstractExecutionContext(ExecutorService centinelExecutor) {
            this.centinelExecutor = centinelExecutor;
            log.info("Initializing ExecutionContext : {}", getClass().getSimpleName());
        }
    }

    /**
     * Retrieves the protocol context for managing protocol-specific operations.
     *
     * @return the protocol context instance associated with this horizon context
     */
    @Override
    public AbstractProtocolContext<I, O> protocolContext() {
        return protocolContext;
    }

    @Override
    public AbstractExecutionContext executionContext() {
        return executionContext;
    }

    @Override
    public AbstractPresentationContext presentationContext() {
        return presentationContext;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
