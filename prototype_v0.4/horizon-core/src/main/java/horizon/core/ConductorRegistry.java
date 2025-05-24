package horizon.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Registry for all Conductors in the system.
 * Manages conductor registration and lookup by intent pattern.
 */
public class ConductorRegistry {
    private final Map<String, Conductor<?, ?>> exactMatches = new ConcurrentHashMap<>();
    private final Map<Pattern, Conductor<?, ?>> patternMatches = new ConcurrentHashMap<>();
    
    /**
     * Registers a conductor for a specific intent pattern.
     *
     * @param conductor the conductor to register
     */
    public void register(Conductor<?, ?> conductor) {
        String pattern = conductor.getIntentPattern();
        
        if (pattern.contains("*") || pattern.contains("?")) {
            // Convert wildcard pattern to regex
            String regex = pattern.replace(".", "\\.")
                                 .replace("*", ".*")
                                 .replace("?", ".");
            patternMatches.put(Pattern.compile(regex), conductor);
        } else {
            // Exact match
            exactMatches.put(pattern, conductor);
        }
    }
    
    /**
     * Finds a conductor for the given intent.
     *
     * @param intent the intent to find a conductor for
     * @return the conductor, or null if none found
     */
    @SuppressWarnings("unchecked")
    public <P, R> Conductor<P, R> find(String intent) {
        // Try exact match first
        Conductor<?, ?> conductor = exactMatches.get(intent);
        if (conductor != null) {
            return (Conductor<P, R>) conductor;
        }
        
        // Try pattern matches
        for (Map.Entry<Pattern, Conductor<?, ?>> entry : patternMatches.entrySet()) {
            if (entry.getKey().matcher(intent).matches()) {
                return (Conductor<P, R>) entry.getValue();
            }
        }
        
        return null;
    }
}
