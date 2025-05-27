package horizon.core.conductor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Cache for ConductorMethod metadata to improve performance.
 * Reduces reflection overhead by caching method analysis results.
 */
public class ConductorMethodCache {
    private static final ConductorMethodCache INSTANCE = new ConductorMethodCache();
    
    private final Map<String, ConductorMethod> methodCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> annotatedParamsCache = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> bodyTypeCache = new ConcurrentHashMap<>();
    
    private ConductorMethodCache() {}
    
    public static ConductorMethodCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Caches a conductor method.
     */
    public void cache(String intent, ConductorMethod method) {
        methodCache.put(intent, method);
        annotatedParamsCache.put(intent, method.hasAnnotatedParameters());
        Class<?> bodyType = method.getBodyParameterType();
        if (bodyType != null) {
            bodyTypeCache.put(intent, bodyType);
        }
    }
    
    /**
     * Gets a cached conductor method.
     */
    public ConductorMethod get(String intent) {
        return methodCache.get(intent);
    }
    
    /**
     * Checks if a method has annotated parameters (cached).
     */
    public Boolean hasAnnotatedParameters(String intent) {
        return annotatedParamsCache.get(intent);
    }
    
    /**
     * Gets the body parameter type (cached).
     */
    public Class<?> getBodyParameterType(String intent) {
        return bodyTypeCache.get(intent);
    }
    
    /**
     * Clears all caches.
     */
    public void clear() {
        methodCache.clear();
        annotatedParamsCache.clear();
        bodyTypeCache.clear();
    }
    
    /**
     * Gets cache statistics.
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "methods", methodCache.size(),
            "annotatedParams", annotatedParamsCache.size(),
            "bodyTypes", bodyTypeCache.size()
        );
    }
}
