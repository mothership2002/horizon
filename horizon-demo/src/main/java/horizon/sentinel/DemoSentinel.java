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
         * Inspects the inbound raw input and logs an informational message indicating its processing.
         *
         * @param rawInput the raw input data to be inspected
         * @throws InboundSentinelException if an error is encountered during the inspection
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("inbound sentinel");
        }
    }

    @Sentinel(direction = Sentinel.SentinelDirection.OUTBOUND, order = 1, scheme = Scheme.http)
    public static class DemoOutboundSentinel<O extends RawOutput> extends AbstractOutboundSentinel<O> {

        /**
         * Inspects outbound data by logging an informational message.
         *
         * @param rawOutput the outbound data to inspect
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
         * Inspects the outbound data by logging a message to indicate processing in a bidirectional sentinel.
         *
         * @param rawOutput the outbound data to be inspected
         * @throws OutboundSentinelException if an error occurs during outbound inspection
         */
        @Override
        public void inspectOutbound(O rawOutput) throws OutboundSentinelException {
            logger.info("both sentinel");
        }

        /**
         * Inspects inbound data for processing within the dual-direction sentinel.
         *
         * <p>This method evaluates the inbound data and logs its processing as part of the sentinel’s
         * combined handling for both inbound and outbound traffic.
         *
         * @param rawInput the inbound data to be inspected
         * @throws InboundSentinelException if an error occurs during inbound data processing
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("both sentinel");
        }
    }

}
