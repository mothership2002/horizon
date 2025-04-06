package horizon.core.flow.rendezvous;

import horizon.core.flow.sentinel.AbstractInboundSentinel;
import horizon.core.flow.sentinel.AbstractOutboundSentinel;
import horizon.core.flow.sentinel.FlowSentinel;
import horizon.core.model.input.RawInput;
import horizon.core.model.output.RawOutput;

import java.util.concurrent.CompletableFuture;

interface ProtocolRendezvous<T extends RawInput, S extends RawOutput> {

    /**
 * Initiates an asynchronous encounter using the provided raw input.
 *
 * <p>This method processes the raw input as part of the protocol rendezvous and returns a 
 * CompletableFuture that completes with the corresponding raw output.</p>
 *
 * @param rawInput the raw input data to be processed
 * @return a CompletableFuture that will eventually yield the protocol's raw output
 */
CompletableFuture<S> encounter(T rawInput);

    /**
 * Registers an inbound sentinel to handle incoming raw input.
 *
 * <p>The specified sentinel will be added to the protocol's inbound processing mechanism,
 * allowing it to participate in the management of incoming data.
 *
 * @param sentinel the inbound sentinel to register
 */
void addInboundSentinel(FlowSentinel.InboundSentinel<T> sentinel);

    /**
 * Registers an outbound sentinel to handle outbound processing.
 *
 * @param sentinel the outbound sentinel that defines how outgoing data is processed
 */
void addOutboundSentinel(FlowSentinel.OutboundSentinel<S> sentinel);
}
