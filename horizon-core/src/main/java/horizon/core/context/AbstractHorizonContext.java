package horizon.core.context;

import horizon.core.constant.Scheme;
import horizon.core.flow.foyer.AbstractProtocolFoyer;
import horizon.core.flow.interpreter.AbstractProtocolInterpreter;
import horizon.core.flow.normalizer.AbstractProtocolNormalizer;
import horizon.core.flow.rendezvous.AbstractProtocolRendezvous;
import horizon.core.model.RawOutputBuilder;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.util.SentinelScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHorizonContext<I extends RawInput, O extends RawOutput> implements HorizonContext<I, O> {

    private final static Logger log = LoggerFactory.getLogger(AbstractHorizonContext.class);

    protected final AbstractProtocolContext<I, O> protocolContext;
    protected final AbstractPresentationContext presentationContext;
    protected final AbstractExecutionContext executionContext;
    protected final Properties properties;
    protected final Scheme scheme;
    protected final SentinelScanner sentinelScanner;

    /**
     * Constructs an AbstractHorizonContext.
     *
     * Initializes the horizon context with the provided protocol, presentation, and execution contexts,
     * configuration properties, operational scheme, and sentinel scanner.
     *
     * @param protocolContext the protocol context for protocol-specific operations
     * @param presentationContext the presentation context for output rendering
     * @param executionContext the execution context for system operations
     * @param properties configuration properties for the context
     * @param scheme the operational scheme defining context behavior
     * @param sentinelScanner the sentinel scanner used for context monitoring and validation
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
         * Constructs a new AbstractProtocolContext using the provided raw output builder and protocol foyer.
         *
         * @param rawOutputBuilder the builder responsible for constructing raw output objects
         * @param foyer the protocol foyer that manages protocol-specific operations
         */
        public AbstractProtocolContext(RawOutputBuilder<O> rawOutputBuilder,
                                       AbstractProtocolFoyer<I> foyer) {

            this.rawOutputBuilder = rawOutputBuilder;
            this.foyer = foyer;
            log.info("Initializing ProtocolContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractPresentationContext implements PresentationContext {

        /**
         * Initializes a new AbstractPresentationContext.
         *
         * <p>This constructor creates an instance of the presentation context and logs an info-level message
         * indicating its initialization using the instance's simple class name.</p>
         */
        public AbstractPresentationContext() {
            log.info("Initializing PresentationContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractExecutionContext implements ExecutionContext {
        /**
         * Constructs an instance of the execution context.
         *
         * <p>This constructor logs the initialization of the execution context using the class's simple name.</p>
         */
        public AbstractExecutionContext() {
            log.info("Initializing ExecutionContext : {}", getClass().getSimpleName());
        }
    }

    /**
     * Retrieves the protocol context component of the current horizon context.
     *
     * @return the {@link AbstractProtocolContext} instance responsible for protocol-related operations
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
