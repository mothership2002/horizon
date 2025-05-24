package horizon.core;

/**
 * A Conductor orchestrates the handling of specific intents.
 * It interprets the intent and payload, conducting the appropriate action.
 *
 * @param <P> the type of payload this conductor can handle
 * @param <R> the type of result this conductor produces
 */
public interface Conductor<P, R> {
    
    /**
     * Conducts the handling of the given payload.
     * This is where business logic is orchestrated.
     *
     * @param payload the payload to process
     * @return the result of conducting the action
     */
    R conduct(P payload);
    
    /**
     * Returns the intent pattern this conductor handles.
     * Can be a simple string or a pattern like "user.*" or "order.create".
     *
     * @return the intent pattern
     */
    String getIntentPattern();
}
