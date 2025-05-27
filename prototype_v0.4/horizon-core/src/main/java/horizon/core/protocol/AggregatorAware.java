package horizon.core.protocol;

import horizon.core.ProtocolAggregator;

/**
 * Interface for protocol adapters that need access to the ProtocolAggregator.
 * This allows adapters to access conductor metadata for advanced features
 * like automatic DTO conversion.
 * 
 * This interface follows the Dependency Inversion Principle - the core
 * defines the interface, and web modules implement it.
 */
public interface AggregatorAware {
    
    /**
     * Sets the protocol aggregator reference.
     * Called by the framework after adapter creation.
     * 
     * @param aggregator the protocol aggregator
     */
    void setProtocolAggregator(ProtocolAggregator aggregator);
}
