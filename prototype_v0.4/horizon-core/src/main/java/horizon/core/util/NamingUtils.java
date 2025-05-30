package horizon.core.util;

/**
 * Utility class for naming conventions and transformations.
 * Provides common methods for converting between different naming styles.
 */
public final class NamingUtils {
    
    /**
     * Converts CamelCase to dot-separated lowercase.
     * Examples:
     * - CreateUser -> create.user
     * - GetUserById -> get.user.by.id
     * - HTTPSConnection -> https.connection
     * 
     * @param camelCase the CamelCase string to convert
     * @return the dot-separated lowercase string
     */
    public static String camelCaseToDotNotation(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        boolean previousWasUppercase = false;
        
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            boolean isUppercase = Character.isUpperCase(c);
            
            if (isUppercase && i > 0) {
                // Check if we should add a dot
                boolean nextIsLowercase = i + 1 < camelCase.length() && 
                    Character.isLowerCase(camelCase.charAt(i + 1));
                boolean shouldAddDot = !previousWasUppercase || nextIsLowercase;
                
                if (shouldAddDot && result.length() > 0) {
                    result.append('.');
                }
            }
            
            result.append(Character.toLowerCase(c));
            previousWasUppercase = isUppercase;
        }
        
        return result.toString();
    }
    
    /**
     * Converts snake_case to CamelCase.
     * Examples:
     * - create_user -> CreateUser
     * - get_user_by_id -> GetUserById
     * 
     * @param snakeCase the snake_case string to convert
     * @return the CamelCase string
     */
    public static String snakeCaseToCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        
        return result.toString();
    }
    
    /**
     * Normalizes a resource name by removing trailing 's' for plurals.
     * Examples:
     * - users -> user
     * - orders -> order
     * - status -> status (no change)
     * 
     * @param resource the resource name to normalize
     * @return the normalized resource name
     */
    public static String normalizePluralResource(String resource) {
        if (resource == null || resource.isEmpty()) {
            return resource;
        }
        
        // Don't remove 's' from words that are naturally singular ending in 's'
        String[] exceptions = {"status", "address", "process", "access", "success"};
        for (String exception : exceptions) {
            if (resource.equalsIgnoreCase(exception)) {
                return resource;
            }
        }
        
        // Remove trailing 's' for plurals
        if (resource.endsWith("s") && resource.length() > 1) {
            return resource.substring(0, resource.length() - 1);
        }
        
        return resource;
    }
    
    /**
     * Extracts action from common method naming patterns.
     * Examples:
     * - CreateUser -> create
     * - GetUserById -> get
     * - UpdateUserProfile -> update
     * 
     * @param methodName the method name to extract action from
     * @return the extracted action, or the original name in lowercase if no pattern matches
     */
    public static String extractAction(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return methodName;
        }
        
        String[] commonPrefixes = {
            "Create", "Get", "Update", "Delete", "List", "Search", 
            "Find", "Save", "Remove", "Add", "Set", "Check", "Validate"
        };
        
        for (String prefix : commonPrefixes) {
            if (methodName.startsWith(prefix)) {
                return prefix.toLowerCase();
            }
        }
        
        // No common prefix found, convert entire name
        return camelCaseToDotNotation(methodName);
    }
    
    // Private constructor to prevent instantiation
    private NamingUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}
