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
     * Constructs a new AbstractHorizonContext with the provided context components and configuration settings.
     *
     * <p>This constructor initializes the protocol, presentation, and execution contexts along with additional
     * configuration properties, a scheme, and a sentinel scanner. It also logs the initialization of the Horizon context instance.
     *
     * @param protocolContext the context managing protocol-related operations
     * @param presentationContext the context managing presentation-related tasks
     * @param executionContext the context managing execution-related operations
     * @param properties the configuration properties for the context
     * @param scheme the scheme defining the context's operational configuration
     * @param sentinelScanner the scanner used for sentinel detection and related validations
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
         * Initializes a new ProtocolContext with the specified raw output builder and protocol foyer.
         *
         * @param rawOutputBuilder the builder used to construct the raw protocol output
         * @param foyer the protocol foyer responsible for handling protocol entry points
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
         * <p>This constructor logs the initialization of the presentation context using its class's simple name.</p>
         */
        public AbstractPresentationContext() {
            log.info("Initializing PresentationContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractExecutionContext implements ExecutionContext {
        /**
         * Instantiates an AbstractExecutionContext and logs its initialization.
         *
         * <p>This constructor logs an informational message indicating that an ExecutionContext is being initialized,
         * using the simple name of the current class.</p>
         */
        public AbstractExecutionContext() {
            log.info("Initializing ExecutionContext : {}", getClass().getSimpleName());
        }
    }

    /**
     * Retrieves the protocol context component responsible for protocol-specific operations.
     *
     * @return the protocol context instance
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
