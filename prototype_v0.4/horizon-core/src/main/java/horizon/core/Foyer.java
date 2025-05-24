package horizon.core;

/**
 * A Foyer is the entry point for a specific protocol.
 * It's where protocol-specific requests first arrive before meeting at the Rendezvous.
 *
 * @param <I> the protocol-specific input type
 */
public interface Foyer<I> {
    
    /**
     * Starts this foyer, beginning to accept requests.
     */
    void open();
    
    /**
     * Stops this foyer, no longer accepting requests.
     */
    void close();
    
    /**
     * Checks if this foyer is currently open.
     *
     * @return true if open, false otherwise
     */
    boolean isOpen();
    
    /**
     * Connects this foyer to a rendezvous point.
     * All requests received by this foyer will be forwarded to the rendezvous.
     *
     * @param rendezvous the rendezvous to connect to
     */
    void connectToRendezvous(Rendezvous<I, ?> rendezvous);
}
