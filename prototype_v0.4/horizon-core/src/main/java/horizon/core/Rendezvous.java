package horizon.core;

/**
 * The central meeting point where all protocols converge.
 * A Rendezvous is responsible for encountering requests and falling away with responses.
 *
 * @param <I> the type of input this rendezvous can encounter
 * @param <O> the type of output this rendezvous produces
 */
public interface Rendezvous<I, O> {
    
    /**
     * Encounters an incoming request from any protocol.
     * This is where different protocols meet and are normalized into a common context.
     *
     * @param input the input from a specific protocol
     * @return a context containing the processed request
     */
    HorizonContext encounter(I input);
    
    /**
     * Falls away with a response after processing.
     * This is where the unified response is adapted back to protocol-specific format.
     *
     * @param context the context containing the processing result
     * @return the output adapted for the originating protocol
     */
    O fallAway(HorizonContext context);
}
