package horizon.web.common;

import java.lang.reflect.Parameter;

/**
 * Resolves method parameters from protocol-specific request data.
 * This interface allows for extensible parameter resolution strategies.
 */
public interface ParameterResolver {
    
    /**
     * Checks if this resolver can handle the given parameter.
     * 
     * @param parameter the method parameter
     * @return true if this resolver can resolve the parameter
     */
    boolean supports(Parameter parameter);
    
    /**
     * Resolves the parameter value from the request context.
     * 
     * @param parameter the method parameter
     * @param context the request context containing all available data
     * @return the resolved parameter value
     * @throws ParameterResolutionException if the parameter cannot be resolved
     */
    Object resolve(Parameter parameter, RequestContext context) throws ParameterResolutionException;
    
    /**
     * Context containing all request data.
     */
    interface RequestContext {
        /**
         * Gets a path parameter by name.
         */
        String getPathParam(String name);
        
        /**
         * Gets a query parameter by name.
         */
        String getQueryParam(String name);
        
        /**
         * Gets all query parameters by name.
         */
        String[] getQueryParams(String name);
        
        /**
         * Gets a header value by name.
         */
        String getHeader(String name);
        
        /**
         * Gets the request body as the specified type.
         */
        <T> T getBody(Class<T> type);
        
        /**
         * Gets the raw request body.
         */
        Object getRawBody();
        
        /**
         * Gets a request attribute.
         */
        Object getAttribute(String name);
    }
    
    /**
     * Exception thrown when parameter resolution fails.
     */
    class ParameterResolutionException extends Exception {
        public ParameterResolutionException(String message) {
            super(message);
        }
        
        public ParameterResolutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
