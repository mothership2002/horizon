package horizon.core.stage;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawOutput;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a stage handler in the Horizon framework.
 * A stage handler is responsible for processing a context and producing
 * raw output based on the context's state and execution result.
 */
public interface StageHandler {

    /**
     * Handles the given context and produces raw output.
     * This method is called when a command has been executed and its result
     * has been stored in the context.
     *
     * @param context the context to handle
     * @return raw output based on the context's state and execution result
     * @throws IllegalArgumentException if the context is invalid
     * @throws NullPointerException if the context is null
     */
    RawOutput handle(HorizonContext context) throws IllegalArgumentException, NullPointerException;

    /**
     * Handles the given context asynchronously and produces raw output.
     * This method is useful for handling contexts that require time-consuming
     * processing without blocking the calling thread.
     *
     * @param context the context to handle
     * @return a CompletableFuture that will be completed with the raw output
     * @throws IllegalArgumentException if the context is invalid
     * @throws NullPointerException if the context is null
     */
    default CompletableFuture<RawOutput> handleAsync(HorizonContext context) throws IllegalArgumentException, NullPointerException {
        return CompletableFuture.supplyAsync(() -> handle(context));
    }

    /**
     * Handles an error that occurred during processing.
     * This method is called when an exception is thrown during processing
     * and is responsible for creating an appropriate error response.
     *
     * @param e the exception that was thrown
     * @param context the context that caused the error
     * @return raw output containing error information
     */
    default RawOutput handleError(Exception e, HorizonContext context) {
        context.setFailureCause(e);
        return context.getRenderedOutput();
    }
}
