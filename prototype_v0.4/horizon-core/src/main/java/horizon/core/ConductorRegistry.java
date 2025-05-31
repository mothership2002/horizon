package horizon.core;

import horizon.core.conductor.ConductorMethod;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Registry for all Conductor methods in the system.
 * Manages conductor method registration and lookup by intent pattern.
 */
public class ConductorRegistry {
    private final Map<String, ConductorMethod> exactMatches = new ConcurrentHashMap<>();
    private final Map<Pattern, ConductorMethod> patternMatches = new ConcurrentHashMap<>();
    
    /**
     * Registers a conductor method for a specific intent.
     *
     * @param method the conductor method to register
     */
    public void register(ConductorMethod method) {
        String intent = method.getIntent();
        
        if (intent.contains("*") || intent.contains("?")) {
            // Convert wildcard pattern to regex
            String regex = intent.replace(".", "\\.")
                                 .replace("*", ".*")
                                 .replace("?", ".");
            patternMatches.put(Pattern.compile(regex), method);
        } else {
            // Exact match
            exactMatches.put(intent, method);
        }
    }
    
    /**
     * Finds a conductor method for the given intent.
     *
     * @param intent the intent to find a conductor for
     * @return the conductor method, or null if none found
     */
    public ConductorMethod find(String intent) {
        // Try exact match first
        ConductorMethod method = exactMatches.get(intent);
        if (method != null) {
            return method;
        }
        
        // Try pattern matches
        for (Map.Entry<Pattern, ConductorMethod> entry : patternMatches.entrySet()) {
            if (entry.getKey().matcher(intent).matches()) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Gets all registered intents.
     * Useful for debugging and discovery.
     */
    public Map<String, ConductorMethod> getAllIntents() {
        return new ConcurrentHashMap<>(exactMatches);
    }
    
    /**
     * Clears all registrations.
     * Useful for testing.
     */
    public void clear() {
        exactMatches.clear();
        patternMatches.clear();
    }
}
