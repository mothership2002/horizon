package horizon.core.flow.sentinel;

import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinel {

    interface OutboundSentinel<O extends RawOutput> extends FlowSentinel {
        /**
 * Inspects the provided outbound raw output.
 *
 * This method evaluates the outbound data to ensure it meets the necessary criteria.
 * If an issue is detected, an OutboundSentinelException is thrown.
 *
 * @param rawOutput the outbound raw data to inspect
 * @throws OutboundSentinelException if the outbound data violates inspection rules
 */
void inspectOutbound(O rawOutput) throws OutboundSentinelException;
    }

    interface InboundSentinel<I extends RawInput> extends FlowSentinel {
        /**
 * Inspects the inbound raw input data.
 *
 * @param rawInput the inbound data to be inspected
 * @throws InboundSentinelException if the inspection determines the data is invalid or cannot be processed
 */
void inspectInbound(I rawInput) throws InboundSentinelException;
    }
}
