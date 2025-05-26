package horizon.web.http.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps intents to their corresponding DTO classes.
 * This is used by the HttpProtocolAdapter to determine which DTO class to use for a given intent.
 */
public class DtoMapper {
    private final Map<String, Class<?>> intentToRequestDtoMap = new HashMap<>();
    
    /**
     * Registers a DTO class for a specific intent.
     * 
     * @param intent the intent
     * @param dtoClass the DTO class
     */
    public void registerRequestDto(String intent, Class<?> dtoClass) {
        intentToRequestDtoMap.put(intent, dtoClass);
    }
    
    /**
     * Gets the DTO class for a specific intent.
     * 
     * @param intent the intent
     * @return the DTO class, or null if none is registered
     */
    public Class<?> getRequestDtoClass(String intent) {
        return intentToRequestDtoMap.get(intent);
    }
    
    /**
     * Checks if a DTO class is registered for a specific intent.
     * 
     * @param intent the intent
     * @return true if a DTO class is registered, false otherwise
     */
    public boolean hasRequestDtoClass(String intent) {
        return intentToRequestDtoMap.containsKey(intent);
    }
}