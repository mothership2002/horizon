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
         * Sets the protocol context for the Horizon context under construction.
         *
         * @param protocolContext the protocol context to configure input/output processing
         * @return the current builder instance for chaining
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
         * Sets the custom properties for configuring the Horizon context.
         *
         * @param properties the properties to be used for context configuration
         * @return the current builder instance for method chaining
         */
        public HorizonContextBuild<I, O> withProperties(Properties properties) {
            this.properties = properties;
            return this;
        }

        /**
         * Assigns the scheme configuration to be used when building the Horizon context.
         *
         * @param scheme the scheme to set for the context configuration
         * @return the current builder instance with the updated scheme
         */
        public HorizonContextBuild<I, O> withScheme(Scheme scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Sets the sentinel scanner for this context builder.
         * 
         * @param sentinelScanner the sentinel scanner to be used during context configuration
         * @return the current builder instance
         */
        public HorizonContextBuild<I, O> withSentinelScanner(SentinelScanner sentinelScanner) {
            this.sentinelScanner = sentinelScanner;
            return this;
        }

        /**
         * Instantiates an AbstractHorizonContext using reflective construction.
         *
         * <p>This method locates a constructor in the specified context class that accepts parameters for protocol,
         * presentation, and execution contexts, along with properties, a scheme, and a sentinel scanner. It then
         * creates a new instance using the builder's current configuration.</p>
         *
         * @param contextClass the class of AbstractHorizonContext to instantiate
         * @return a new instance of the specified AbstractHorizonContext configured with the current builder parameters
         * @throws NoSuchMethodException if the required constructor is not found
         * @throws InvocationTargetException if the constructor throws an exception during instantiation
         * @throws InstantiationException if the specified contextClass represents an abstract class
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
