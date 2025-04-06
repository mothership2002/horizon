package horizon.core.util;

import horizon.core.constant.Scheme;
import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.Properties;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class HorizonContextBuilder {

    public static <I extends RawInput, O extends RawOutput> HorizonContextBuild<I, O> builder() {
        return new HorizonContextBuild<>();
    }

    public static class HorizonContextBuild<I extends RawInput, O extends RawOutput> {

        private AbstractHorizonContext.AbstractProtocolContext<I, O> protocolContext;
        private AbstractHorizonContext.AbstractPresentationContext presentationContext;
        private AbstractHorizonContext.AbstractExecutionContext executionContext;
        private Scheme scheme;
        private SentinelScanner sentinelScanner;
        private Properties properties;

        /**
         * Sets the protocol context for the Horizon context being built.
         *
         * @param protocolContext the protocol context configuration used for protocol-level settings
         * @return the current builder instance for fluent method chaining
         */
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

        /**
         * Sets the configuration properties for the context being built.
         *
         * <p>This method assigns the given properties to the builder, which will be used during the
         * instantiation of the context. It supports a fluent interface by returning the builder
         * instance.
         *
         * @param properties the configuration properties to apply
         * @return the current builder instance
         */
        public HorizonContextBuild<I, O> withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Sets the scheme to be used in building the Horizon context.
         *
         * @param scheme the scheme configuration to apply
         * @return the current builder instance for method chaining
         */
        public HorizonContextBuild<I, O> withScheme(Scheme scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Configures the builder with a SentinelScanner instance.
         *
         * @param sentinelScanner the SentinelScanner to be used during context instantiation
         * @return the updated builder instance
         */
        public HorizonContextBuild<I, O> withSentinelScanner(SentinelScanner sentinelScanner) {
            this.sentinelScanner = sentinelScanner;
            return this;
        }

        /**
         * Constructs a new instance of the specified AbstractHorizonContext using the builder's configured components.
         *
         * <p>This method looks up a constructor in the provided context class that accepts parameters for the protocol context,
         * presentation context, execution context, properties, scheme, and sentinel scanner, and then creates a new instance using
         * these values.
         *
         * @param contextClass the concrete subclass of AbstractHorizonContext to instantiate
         * @return a newly created AbstractHorizonContext instance configured with the builder's components
         * @throws NoSuchMethodException if the expected constructor is not found in the context class
         * @throws InvocationTargetException if the constructor invocation results in an exception
         * @throws InstantiationException if the context class cannot be instantiated (e.g., if it is abstract)
         * @throws IllegalAccessException if the constructor is inaccessible
         */
        public AbstractHorizonContext<I, O> build(Class<? extends AbstractHorizonContext<I, O>> contextClass)
                throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

            Constructor<? extends AbstractHorizonContext<I, O>> constructor = contextClass.getDeclaredConstructor(AbstractHorizonContext.AbstractProtocolContext.class,
                    AbstractHorizonContext.AbstractPresentationContext.class,
                    AbstractHorizonContext.AbstractExecutionContext.class,
                    Properties.class, Scheme.class, SentinelScanner.class);
            return constructor
                    .newInstance(protocolContext, presentationContext, executionContext, properties, scheme, sentinelScanner);
        }
    }
}
