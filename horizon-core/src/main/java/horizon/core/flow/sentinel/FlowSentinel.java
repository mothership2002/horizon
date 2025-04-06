package horizon.core.flow.sentinel;

import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinel {

    interface OutboundSentinel<O extends RawOutput> extends FlowSentinel {
        /**
 * Inspects the outbound raw data for compliance with expected standards.
 *
 * <p>This method examines the provided outbound data and validates its structure and content.
 * If anomalies or issues are detected during the inspection, an OutboundSentinelException is thrown.</p>
 *
 * @param rawOutput the raw outbound data to be inspected
 * @throws OutboundSentinelException if the data fails to meet the expected criteria
 */
void inspectOutbound(O rawOutput) throws OutboundSentinelException;
    }

    interface InboundSentinel<I extends RawInput> extends FlowSentinel {
        /**
 * Inspects the inbound raw input data.
 *
 * @param rawInput the inbound input data to inspect
 * @throws InboundSentinelException if the input data fails inspection
 */
void inspectInbound(I rawInput) throws InboundSentinelException;
    }
}
