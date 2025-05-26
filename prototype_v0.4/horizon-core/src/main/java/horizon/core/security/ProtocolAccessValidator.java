package horizon.core.security;

import horizon.core.annotation.ProtocolAccess;
import horizon.core.annotation.ProtocolSchema;
import horizon.core.conductor.ConductorMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates protocol access to conductors based on @ProtocolAccess annotations.
 * 
 * Access is determined by:
 * 1. @ProtocolAccess with schema definitions (the highest priority)
 * 2. @ProtocolAccess with a value list (simple access control)
 * 3. No restrictions (backward compatibility)
 */
public class ProtocolAccessValidator {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolAccessValidator.class);
    
    /**
     * Checks if a protocol has access to a conductor method.
     */
    public boolean hasAccess(String protocol, ConductorMethod conductorMethod) {
        Method method = conductorMethod.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        
        // Check method-level @ProtocolAccess
        ProtocolAccess methodAccess = method.getAnnotation(ProtocolAccess.class);
        if (methodAccess != null) {
            Boolean access = checkProtocolAccess(protocol, methodAccess);
            if (access != null) return access;
        }
        
        // Check class-level @ProtocolAccess
        ProtocolAccess classAccess = declaringClass.getAnnotation(ProtocolAccess.class);
        if (classAccess != null) {
            Boolean access = checkProtocolAccess(protocol, classAccess);
            if (access != null) return access;
        }
        
        // No access control defined - allow for backward compatibility
        logger.warn("No protocol access control found for {}.{} - allowing all protocols (backward compatibility)", 
                   declaringClass.getSimpleName(), method.getName());
        return true;
    }
    
    /**
     * Checks access based on the @ProtocolAccess annotation.
     * 
     * @return Boolean.TRUE if allowed, Boolean. FALSE if denied, null if not determined
     */
    private Boolean checkProtocolAccess(String protocol, ProtocolAccess access) {
        // First check schema-based access
        ProtocolSchema[] schemas = access.schema();
        if (schemas.length > 0) {
            for (ProtocolSchema schema : schemas) {
                if (protocol.equals(schema.protocol())) {
                    return true;
                }
            }
            // Has schemas but not for this protocol
            return false;
        }
        
        // Check value() attribute (simple allowlist)
        String[] allowList = access.value();
        if (allowList.length > 0) {
            Set<String> allowedProtocols = new HashSet<>(Arrays.asList(allowList));
            if (allowedProtocols.contains(protocol)) {
                return true;
            }
            return access.allowOthers();
        }
        
        // No specific configuration
        return null;
    }
    
    /**
     * Gets the protocol schema for a specific protocol from a method.
     * 
     * @return the schema string or null if not found
     */
    public String getProtocolSchema(String protocol, Method method) {
        // Check method-level @ProtocolAccess
        ProtocolAccess methodAccess = method.getAnnotation(ProtocolAccess.class);
        if (methodAccess != null) {
            String schema = findSchemaValue(protocol, methodAccess);
            if (schema != null) return schema;
        }
        
        // Check class-level @ProtocolAccess
        ProtocolAccess classAccess = method.getDeclaringClass().getAnnotation(ProtocolAccess.class);
        if (classAccess != null) {
            return findSchemaValue(protocol, classAccess);
        }
        
        return null;
    }
    
    private String findSchemaValue(String protocol, ProtocolAccess access) {
        for (ProtocolSchema schema : access.schema()) {
            if (protocol.equals(schema.protocol())) {
                return schema.value();
            }
        }
        return null;
    }
}
