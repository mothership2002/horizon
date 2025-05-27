package horizon.web.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for matching URL paths against patterns and extracting parameters.
 */
public class PathMatcher {
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^/]+)\\}");
    
    private final String pattern;
    private final Pattern regex;
    private final String[] paramNames;
    
    public PathMatcher(String pattern) {
        this.pattern = pattern;
        
        // Extract parameter names
        Matcher matcher = PARAM_PATTERN.matcher(pattern);
        StringBuilder paramNameBuilder = new StringBuilder();
        int count = 0;
        while (matcher.find()) {
            if (count > 0) paramNameBuilder.append(",");
            paramNameBuilder.append(matcher.group(1));
            count++;
        }
        this.paramNames = count > 0 ? paramNameBuilder.toString().split(",") : new String[0];
        
        // Convert pattern to regex
        String regexPattern = pattern.replaceAll("\\{[^/]+\\}", "([^/]+)");
        this.regex = Pattern.compile("^" + regexPattern + "$");
    }
    
    /**
     * Matches a path against the pattern and extracts parameters.
     * 
     * @param path the path to match
     * @return a map of parameter names to values, or null if no match
     */
    public Map<String, String> match(String path) {
        Matcher matcher = regex.matcher(path);
        if (!matcher.matches()) {
            return null;
        }
        
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < paramNames.length; i++) {
            params.put(paramNames[i], matcher.group(i + 1));
        }
        return params;
    }
    
    public String getPattern() {
        return pattern;
    }
}
