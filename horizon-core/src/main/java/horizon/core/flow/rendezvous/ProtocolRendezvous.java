package horizon.core.flow.rendezvous;

import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolRendezvous<T extends RawInput, S extends RawOutput> {

    /**
 * Processes the provided raw input asynchronously.
 *
 * <p>This method initiates the asynchronous processing of the given raw input and returns a
 * CompletableFuture that will eventually complete with its corresponding raw output.</p>
 *
 * @param rawInput the raw input data to be processed
 * @return a CompletableFuture that will complete with the processed raw output
 */
CompletableFuture<S> encounter(T rawInput);

    /**
 * Registers an inbound sentinel for intercepting and processing incoming raw input.
 *
 * <p>The provided sentinel enables additional validation, transformation, or filtering of raw input
 * before it is further handled by the protocol.
 *
 * @param sentinel the inbound sentinel to register
 */
void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel);

    /**
 * Registers an outbound sentinel to process and validate outbound raw output data.
 *
 * @param sentinel the outbound sentinel to add for handling output data elements of type S
 */
void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel);
}
