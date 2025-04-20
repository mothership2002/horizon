package horizon.core.conductor;

import horizon.core.command.Command;

import java.util.Collections;
import java.util.List;

/**
 * Represents a conductor in the Horizon framework.
 * A conductor is responsible for resolving a payload into a command
 * that can be executed to fulfill the intent represented by the payload.
 *
 * @param <P> the type of payload this conductor can resolve
 */
public interface Conductor<P> {

    /**
     * Resolves the given payload into a command.
     * This method analyzes the payload and determines the appropriate
     * command to execute based on the intent represented by the payload.
     *
     * @param payload the payload to resolve
     * @return a command that can fulfill the intent represented by the payload
     * @throws IllegalArgumentException if the payload is invalid or cannot be resolved
     * @throws NullPointerException if the payload is null
     */
    Command<?> resolve(P payload) throws IllegalArgumentException, NullPointerException;

    /**
     * Resolves the given payload into multiple commands.
     * This method is useful when a single payload represents multiple intents
     * that need to be fulfilled by different commands.
     *
     * @param payload the payload to resolve
     * @return a list of commands that can fulfill the intents represented by the payload
     * @throws IllegalArgumentException if the payload is invalid or cannot be resolved
     * @throws NullPointerException if the payload is null
     */
    default List<Command<?>> resolveMultiple(P payload) throws IllegalArgumentException, NullPointerException {
        return Collections.singletonList(resolve(payload));
    }
}
