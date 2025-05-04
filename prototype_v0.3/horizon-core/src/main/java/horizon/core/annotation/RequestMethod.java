package horizon.core.annotation;

/**
 * Enumeration of HTTP request methods.
 * Used by {@link Intent} annotation to specify which HTTP methods a handler should process.
 */
public enum RequestMethod {
    /**
     * HTTP GET method.
     */
    GET,
    
    /**
     * HTTP POST method.
     */
    POST,
    
    /**
     * HTTP PUT method.
     */
    PUT,
    
    /**
     * HTTP DELETE method.
     */
    DELETE,
    
    /**
     * HTTP PATCH method.
     */
    PATCH,
    
    /**
     * HTTP HEAD method.
     */
    HEAD,
    
    /**
     * HTTP OPTIONS method.
     */
    OPTIONS,
    
    /**
     * HTTP TRACE method.
     */
    TRACE
}