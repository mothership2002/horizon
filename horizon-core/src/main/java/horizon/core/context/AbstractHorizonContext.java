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

        public AbstractProtocolContext(RawOutputBuilder<O> rawOutputBuilder,
                                       AbstractProtocolFoyer<I> foyer) {

            this.rawOutputBuilder = rawOutputBuilder;
            this.foyer = foyer;
            log.info("Initializing ProtocolContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractPresentationContext implements PresentationContext {
        protected final ExecutorService conductorExecutor;

        public AbstractPresentationContext(ExecutorService conductorExecutor) {
            this.conductorExecutor = conductorExecutor;
            log.info("Initializing PresentationContext : {}", getClass().getSimpleName());
        }
    }

    public abstract static class AbstractExecutionContext implements ExecutionContext {
        protected final ExecutorService centinelExecutor;

        public AbstractExecutionContext(ExecutorService centinelExecutor) {
            this.centinelExecutor = centinelExecutor;
            log.info("Initializing ExecutionContext : {}", getClass().getSimpleName());
        }
    }

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
