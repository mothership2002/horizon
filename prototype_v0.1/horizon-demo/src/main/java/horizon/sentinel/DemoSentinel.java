package horizon.sentinel;

import horizon.core.annotation.Sentinel;
import horizon.core.constant.Scheme;
import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.sentinel.AbstractSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DemoSentinel {

    private static final Logger logger = LoggerFactory.getLogger(DemoSentinel.class.getName());

    @Sentinel(direction = Sentinel.SentinelDirection.INBOUND, order = 1, scheme = Scheme.http)
    public static class DemoInboundSentinel<I extends RawInput> extends AbstractInboundSentinel<I> {

        /**
         * Inspects the inbound raw input by logging an inbound sentinel event.
         *
         * @param rawInput the raw inbound data to be inspected
         * @throws InboundSentinelException if processing the inbound data fails
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("inbound sentinel");
        }
    }

    @Sentinel(direction = Sentinel.SentinelDirection.OUTBOUND, order = 1, scheme = Scheme.http)
    public static class DemoOutboundSentinel<O extends RawOutput> extends AbstractOutboundSentinel<O> {

        /**
         * Processes outbound data by logging an informational message.
         *
         * <p>This method is part of the outbound sentinel workflow and signals that outbound data inspection has been initiated.
         *
         * @param rawOutput the outbound data to be inspected
         * @throws OutboundSentinelException if an error occurs during outbound inspection
         */
        @Override
        public void inspectOutbound(O rawOutput) throws OutboundSentinelException {
            logger.info("outbound sentinel");
        }
    }

    @Sentinel(direction = Sentinel.SentinelDirection.BOTH, order = 0, scheme = Scheme.http)
    public static class DemoBothSentinel<I extends RawInput, O extends RawOutput> extends AbstractSentinel<I, O> {

        /**
         * Inspects and processes outbound data.
         *
         * <p>This implementation logs an informational message signaling that outbound data is being
         * handled by this sentinel. It serves as the outbound inspection method for the sentinel that
         * supports both inbound and outbound processing.
         *
         * @param rawOutput the outbound data to inspect
         * @throws OutboundSentinelException if an error occurs during outbound data inspection
         */
        @Override
        public void inspectOutbound(O rawOutput) throws OutboundSentinelException {
            logger.info("both sentinel");
        }

        /**
         * Inspects inbound data as part of the both-sentinel processing.
         *
         * <p>This implementation logs an informational message indicating that inbound data is being processed.
         *
         * @param rawInput the inbound data to inspect
         * @throws InboundSentinelException if an error occurs during inspection
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("both sentinel");
        }
    }

}
