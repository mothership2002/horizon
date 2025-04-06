package horizon.core.flow.sentinel;

import horizon.core.exception.InboundSentinelException;
import horizon.core.exception.OutboundSentinelException;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

public interface FlowSentinel {

    interface OutboundSentinel<O extends RawOutput> extends FlowSentinel {
        /**
 * Inspects the outbound raw output data.
 * <p>
 * Implementations should examine the provided data for compliance with
 * the system's outbound policies. If the data is found to be invalid or
 * violates sentinel rules, an {@link OutboundSentinelException} will be thrown.
 *
 * @param rawOutput the outbound raw output data to inspect
 * @throws OutboundSentinelException if the outbound data fails inspection
 */
void inspectOutbound(O rawOutput) throws OutboundSentinelException;
    }

    interface InboundSentinel<I extends RawInput> extends FlowSentinel {
        /**
 * Inspects the inbound raw input data.
 *
 * <p>This method validates the provided raw input data. Implementations should throw an
 * InboundSentinelException if the input fails the necessary inspection criteria.</p>
 *
 * @param rawInput the inbound raw input data to inspect
 * @throws InboundSentinelException if the inbound data does not meet the required criteria
 */
void inspectInbound(I rawInput) throws InboundSentinelException;
    }
}
