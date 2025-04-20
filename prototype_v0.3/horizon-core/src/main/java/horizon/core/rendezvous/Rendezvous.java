package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

/**
 * Represents a rendezvous point in the Horizon framework.
 * A rendezvous is responsible for creating a context from raw input
 * and finalizing the output from a context.
 *
 * @param <I> the type of raw input this rendezvous can handle
 * @param <O> the type of raw output this rendezvous produces
 */
public interface Rendezvous<I extends RawInput, O extends RawOutput> {

    /**
     * Creates a context from the given raw input.
     * This method is called when input is received and is responsible for
     * initializing the context with the appropriate data.
     *
     * @param input the raw input to process
     * @return a context initialized with data from the input
     * @throws IllegalArgumentException if the input is invalid
     * @throws NullPointerException if the input is null
     */
    HorizonContext encounter(I input) throws IllegalArgumentException, NullPointerException;

    /**
     * Finalizes the output from the given context.
     * This method is called when processing is complete and is responsible for
     * converting the context's result into raw output.
     *
     * @param context the context containing the result
     * @return raw output containing the finalized result
     * @throws IllegalArgumentException if the context is invalid
     * @throws NullPointerException if the context is null
     */
    O fallAway(HorizonContext context) throws IllegalArgumentException, NullPointerException;

    /**
     * Handles an error that occurred during processing.
     * This method is called when an exception is thrown during processing
     * and is responsible for creating an appropriate error response.
     *
     * @param e the exception that was thrown
     * @param input the raw input that caused the error
     * @return a context initialized with error information
     */
    default HorizonContext handleError(Exception e, I input) {
        HorizonContext context = new HorizonContext(input);
        context.setFailureCause(e);
        return context;
    }
}
