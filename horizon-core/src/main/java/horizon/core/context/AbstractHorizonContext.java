package horizon.core.context;

import horizon.core.conductor.AbstractConductorManager;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import horizon.core.stage.AbstractShadowStage;

public abstract class AbstractHorizonContext<I extends RawInput, O extends RawOutput> implements HorizonContext<I, O> {

    protected final AbstractProtocolContext<I, O> protocolContext;
    protected final AbstractPresentationContext presentationContext;
    protected final AbstractExecutionContext executionContext;
    protected final Properties properties;

    public AbstractHorizonContext(AbstractProtocolContext<I, O> protocolContext, AbstractPresentationContext presentationContext, AbstractExecutionContext executionContext, Properties properties) {
        this.protocolContext = protocolContext;
        this.presentationContext = presentationContext;
        this.executionContext = executionContext;
        this.properties = properties;
    }

    public abstract static class AbstractProtocolContext<I extends RawInput, O extends RawOutput> implements ProtocolContext<I, O> {
        protected AbstractConductorManager conductorManager;
        protected AbstractShadowStage shadowStage;

        public AbstractProtocolContext(AbstractConductorManager conductorManager, AbstractShadowStage shadowStage) {
            this.conductorManager = conductorManager;
            this.shadowStage = shadowStage;
        }
    }

    public abstract static class AbstractPresentationContext implements PresentationContext {

    }

    public abstract static class AbstractExecutionContext implements ExecutionContext {

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
