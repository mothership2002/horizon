package horizon.core.flow.rendezvous;

import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolRendezvous<T extends RawInput, S extends RawOutput> {

    /**
 * Initiates asynchronous processing for the provided raw input.
 *
 * <p>This method receives raw input data and returns a CompletableFuture
 * that will eventually complete with the corresponding processed output.
 *
 * @param rawInput the raw input data to be processed
 * @return a CompletableFuture that completes with the processed output
 */
CompletableFuture<S> encounter(T rawInput);

    /**
 * Adds an inbound sentinel for monitoring incoming raw input data.
 *
 * @param sentinel the inbound sentinel that oversees the flow of incoming data of type {@code T}
 */
void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel);

    /**
 * Adds an outbound sentinel to monitor and manage the flow of outgoing data.
 *
 * @param sentinel the outbound sentinel responsible for overseeing outbound data processing
 */
void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel);
}
