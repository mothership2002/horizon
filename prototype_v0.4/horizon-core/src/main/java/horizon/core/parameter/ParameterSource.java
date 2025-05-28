package horizon.core.parameter;

/**
 * Source of parameter value.
 */
public enum ParameterSource {
    PATH,    // From URL path
    QUERY,   // From query string
    HEADER,  // From HTTP header
    BODY     // From the request body
}