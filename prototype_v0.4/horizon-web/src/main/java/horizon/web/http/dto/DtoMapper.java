package horizon.web.http.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps intents to DTO (Data Transfer Object) classes.
 * 
 * @deprecated As of version 0.5, replaced by automatic DTO resolution 
 *             based on conductor method parameters. The framework now
 *             automatically detects and uses the appropriate DTO type
 *             from the conductor method signature.
 * 
 * @see horizon.web.common.PayloadExtractor
 */
@Deprecated(since = "0.5", forRemoval = true)
public class DtoMapper {
    private final Map<String, Class<?>> requestDtoClasses = new HashMap<>();
    
    /**
     * Registers a DTO class for a specific intent.
     * 
     * @param intent the intent to map
     * @param dtoClass the DTO class to map to the intent
     * @return this DtoMapper instance for method chaining
     */
    public DtoMapper registerRequestDto(String intent, Class<?> dtoClass) {
        requestDtoClasses.put(intent, dtoClass);
        return this;
    }
    
    /**
     * Checks if a DTO class is registered for the given intent.
     * 
     * @param intent the intent to check
     * @return true if a DTO class is registered for the intent, false otherwise
     */
    public boolean hasRequestDtoClass(String intent) {
        return requestDtoClasses.containsKey(intent);
    }
    
    /**
     * Gets the DTO class registered for the given intent.
     * 
     * @param intent the intent to get the DTO class for
     * @return the DTO class registered for the intent, or null if none is registered
     */
    public Class<?> getRequestDtoClass(String intent) {
        return requestDtoClasses.get(intent);
    }
}
