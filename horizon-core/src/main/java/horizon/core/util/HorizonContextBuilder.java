package horizon.core.util;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.Properties;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class HorizonContextBuilder {

    public static <I extends RawInput, O extends RawOutput> HorizonContextBuild<I, O> builder() {
        return new HorizonContextBuild<>();
    }

    public static class HorizonContextBuild<I extends RawInput, O extends RawOutput> {

        private AbstractHorizonContext.AbstractProtocolContext<I, O> protocolContext;
        private AbstractHorizonContext.AbstractPresentationContext presentationContext;
        private AbstractHorizonContext.AbstractExecutionContext executionContext;
        private Properties properties;

        public HorizonContextBuild<I, O> withProtocolContext(AbstractHorizonContext.AbstractProtocolContext<I, O> protocolContext) {
            this.protocolContext = protocolContext;
            return this;
        }

        public HorizonContextBuild<I, O> withPresentationContext(AbstractHorizonContext.AbstractPresentationContext presentationContext) {
            this.presentationContext = presentationContext;
            return this;
        }

        public HorizonContextBuild<I, O> withExecutionContext(AbstractHorizonContext.AbstractExecutionContext executionContext) {
            this.executionContext = executionContext;
            return this;
        }

        public HorizonContextBuild<I, O> withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public AbstractHorizonContext<I, O> build(Class<? extends AbstractHorizonContext<I, O>> contextClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Constructor<? extends AbstractHorizonContext<I, O>> constructor = contextClass.getDeclaredConstructor(AbstractHorizonContext.AbstractProtocolContext.class,
                    AbstractHorizonContext.AbstractPresentationContext.class,
                    AbstractHorizonContext.AbstractExecutionContext.class,
                    Properties.class);
            return constructor
                    .newInstance(protocolContext, presentationContext, executionContext, properties);
        }
    }
}
