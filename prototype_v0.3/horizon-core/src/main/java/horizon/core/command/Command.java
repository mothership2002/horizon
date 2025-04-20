package horizon.core.command;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a command in the Horizon framework.
 * A command encapsulates a specific action to be executed and provides
 * methods for executing the action and retrieving the command's key.
 *
 * @param <T> the type of result returned by this command
 */
public interface Command<T> {

    /**
     * Executes this command and returns the result.
     * This method is synchronous and will block until the command completes.
     *
     * @return the result of executing this command
     * @throws Exception if an error occurs during execution
     */
    T execute() throws Exception;

    /**
     * Executes this command asynchronously and returns a CompletableFuture
     * that will be completed with the result when the command completes.
     *
     * @return a CompletableFuture that will be completed with the result
     */
    default CompletableFuture<T> executeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Returns the key of this command.
     * The key uniquely identifies the command and is used for routing
     * and handling the command's result.
     *
     * @return a non-null String representing the key
     */
    String getKey();
}
