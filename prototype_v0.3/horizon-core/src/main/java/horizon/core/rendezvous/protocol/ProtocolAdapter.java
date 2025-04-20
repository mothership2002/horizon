package horizon.core.rendezvous.protocol;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

/**
 * Adapter interface for converting between protocol-specific messages and Horizon's RawInput/RawOutput.
 * This interface serves as a bridge between a specific protocol implementation and the Horizon framework.
 *
 * @param <I> the type of raw input this adapter produces
 * @param <O> the type of raw output this adapter consumes
 * @param <M> the type of protocol-specific incoming message
 * @param <R> the type of protocol-specific outgoing response
 */
public interface ProtocolAdapter<I extends RawInput, O extends RawOutput, M, R> {

    /**
     * Converts a protocol-specific message to a Horizon raw input.
     *
     * @param message the protocol-specific message to convert
     * @param remoteAddress the remote address of the client
     * @return the converted raw input
     * @throws IllegalArgumentException if the message cannot be converted
     * @throws NullPointerException if message is null
     */
    I convertToInput(M message, String remoteAddress) throws IllegalArgumentException, NullPointerException;

    /**
     * Converts a Horizon raw output to a protocol-specific response.
     *
     * @param output the raw output to convert
     * @param context any additional context needed for the conversion
     * @return the protocol-specific response
     * @throws IllegalArgumentException if the output cannot be converted
     * @throws NullPointerException if output is null
     */
    R convertToResponse(O output, Object context) throws IllegalArgumentException, NullPointerException;

    /**
     * Creates an error response for the given exception.
     *
     * @param e the exception that caused the error
     * @param context any additional context needed for the conversion
     * @return the protocol-specific error response
     */
    R createErrorResponse(Throwable e, Object context);

    /**
     * Creates a forbidden response.
     *
     * @param context any additional context needed for the conversion
     * @return the protocol-specific forbidden response
     */
    R createForbiddenResponse(Object context);
}