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
         * Configures the protocol context for the Horizon context.
         *
         * <p>
         * Assigns the specified protocol context—which encapsulates the protocol configuration—to the builder.
         * The current builder instance is returned to support fluent method chaining.
         * </p>
         *
         * @param protocolContext the protocol-specific configuration for the Horizon context
         * @return the updated builder instance
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
         * Sets the configuration properties for the context to be built.
         *
         * @param properties the configuration properties to use during context initialization
         * @return the current builder instance for chaining
         */
        public HorizonContextBuild<I, O> withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Sets the scheme to be used during the context construction.
         *
         * @param scheme the scheme to configure with this builder
         * @return the builder instance for chaining configuration methods
         */
        public HorizonContextBuild<I, O> withScheme(Scheme scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Configures the builder with the specified SentinelScanner.
         *
         * @param sentinelScanner the SentinelScanner instance used for sentinel scanning
         * @return the current HorizonContextBuild instance for method chaining
         */
        public HorizonContextBuild<I, O> withSentinelScanner(SentinelScanner sentinelScanner) {
            this.sentinelScanner = sentinelScanner;
            return this;
        }

        /**
         * Constructs an instance of AbstractHorizonContext using reflection.
         *
         * <p>This method retrieves a constructor from the specified context class that accepts
         * parameters for protocol, presentation, and execution contexts, along with properties,
         * scheme, and a sentinel scanner. It then instantiates and returns a new context instance
         * configured with these components.</p>
         *
         * @param contextClass the class of AbstractHorizonContext to instantiate; it must declare a constructor
         *                     with parameters in the following order: AbstractProtocolContext,
         *                     AbstractPresentationContext, AbstractExecutionContext, Properties, Scheme, SentinelScanner.
         * @return a new instance of AbstractHorizonContext configured with the current settings
         * @throws NoSuchMethodException if the expected constructor is not found in the specified context class
         * @throws InvocationTargetException if the constructor invocation fails
         * @throws InstantiationException if instantiation of the context fails
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
