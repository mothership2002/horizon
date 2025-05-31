package horizon.core.parameter;

/**
 * Source of parameter value.
 */
public enum ParameterSource {
    PATH,    // From URL path (HTTP-specific, deprecated)
    QUERY,   // From query string (HTTP-specific, deprecated)
    HEADER,  // From HTTP header (Protocol-specific, deprecated)
    BODY,    // From the request body (deprecated)
    
    // Protocol-neutral sources
    PARAM,   // Protocol-neutral parameter (@Param annotation)
    AUTO     // Auto-detect from context
}
