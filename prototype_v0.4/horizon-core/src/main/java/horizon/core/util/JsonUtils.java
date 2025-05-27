package horizon.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON operations.
 * Provides a centralized ObjectMapper instance to be used across the application.
 * This helps improve performance by avoiding the creation of multiple ObjectMapper instances.
 */
public class JsonUtils {
    
    /**
     * Singleton instance of ObjectMapper.
     * ObjectMapper is thread-safe and can be reused across multiple threads.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * Gets the shared ObjectMapper instance.
     * 
     * @return the shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
    
    /**
     * Converts an object to its JSON string representation.
     * 
     * @param value the object to convert
     * @return the JSON string representation
     * @throws Exception if conversion fails
     */
    public static String toJson(Object value) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(value);
    }
    
    /**
     * Parses a JSON string into an object of the specified type.
     * 
     * @param json the JSON string to parse
     * @param valueType the class of the object to parse into
     * @param <T> the type of the object
     * @return the parsed object
     * @throws Exception if parsing fails
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws Exception {
        return OBJECT_MAPPER.readValue(json, valueType);
    }
    
    /**
     * Converts a value to a specified type.
     * 
     * @param value the value to convert
     * @param valueType the class of the object to convert to
     * @param <T> the type of the object
     * @return the converted object
     */
    public static <T> T convertValue(Object value, Class<T> valueType) {
        return OBJECT_MAPPER.convertValue(value, valueType);
    }
    
    // Private constructor to prevent instantiation
    private JsonUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}