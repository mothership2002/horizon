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
         * Inspects inbound data by logging an informational message.
         *
         * @param rawInput the raw inbound data to be processed
         * @throws InboundSentinelException if an error occurs during the inspection of inbound data
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("inbound sentinel");
        }
    }

    @Sentinel(direction = Sentinel.SentinelDirection.OUTBOUND, order = 1, scheme = Scheme.http)
    public static class DemoOutboundSentinel<O extends RawOutput> extends AbstractOutboundSentinel<O> {

        /**
         * Inspects the outbound data.
         *
         * <p>This method logs a message indicating that outbound data is being processed. It is part of the outbound
         * sentinel implementation and may throw an {@code OutboundSentinelException} if inspection fails.</p>
         *
         * @param rawOutput the outbound data to inspect
         * @throws OutboundSentinelException if an error occurs during outbound data inspection
         */
        @Override
        public void inspectOutbound(O rawOutput) throws OutboundSentinelException {
            logger.info("outbound sentinel");
        }
    }

    @Sentinel(direction = Sentinel.SentinelDirection.BOTH, order = 0, scheme = Scheme.http)
    public static class DemoBothSentinel<I extends RawInput, O extends RawOutput> extends AbstractSentinel<I, O> {

        /**
         * Processes outbound data by logging that the dual-direction sentinel is handling outbound traffic.
         *
         * <p>This method is part of a sentinel that inspects both inbound and outbound data. It logs an informational
         * message to indicate that outbound data has been processed.</p>
         *
         * @param rawOutput the outbound data to inspect
         * @throws OutboundSentinelException if an error occurs during outbound data inspection
         */
        @Override
        public void inspectOutbound(O rawOutput) throws OutboundSentinelException {
            logger.info("both sentinel");
        }

        /**
         * Inspects inbound data as part of the dual-mode sentinel operation.
         *
         * <p>This method processes inbound data, logging an informational message to indicate that the inbound branch
         * of the sentinel has been activated.
         *
         * @param rawInput the inbound data to inspect
         * @throws InboundSentinelException if an error occurs during inbound data inspection
         */
        @Override
        public void inspectInbound(I rawInput) throws InboundSentinelException {
            logger.info("both sentinel");
        }
    }

}
