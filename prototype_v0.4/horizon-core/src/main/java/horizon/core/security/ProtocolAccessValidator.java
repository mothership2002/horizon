package horizon.core.security;

import horizon.core.annotation.*;
import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates protocol access to conductors based on protocol access annotations.
 * 
 * Access is determined by:
 * 1. @ProtocolAccess with schema definitions (highest priority)
 * 2. @ProtocolAccess with allow list
 * 3. Legacy @HttpResource/@WebSocketResource annotations (deprecated)
 * 4. @ProtocolMapping annotations (deprecated)
 * 5. No restrictions (backward compatibility)
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
        
        // Check legacy convenience annotations (deprecated)
        if (hasLegacyMapping(method, protocol)) {
            logger.debug("Access granted to {} for {} via legacy annotation", protocol, method.getName());
            return true;
        }
        
        // Check @ProtocolMapping annotations (deprecated)
        if (hasProtocolMapping(method, protocol)) {
            logger.debug("Access granted to {} for {} via @ProtocolMapping", protocol, method.getName());
            return true;
        }
        
        // No access control defined - check if any mapping exists
        boolean hasAnyMapping = hasAnyProtocolMapping(method);
        if (hasAnyMapping) {
            // Has mappings but not for this protocol - deny access
            logger.debug("Access denied to {} for {} - has mappings but not for this protocol", 
                        protocol, method.getName());
            return false;
        }
        
        // No mappings at all - allow for backward compatibility
        logger.warn("No protocol access control found for {}.{} - allowing all protocols (backward compatibility)", 
                   declaringClass.getSimpleName(), method.getName());
        return true;
    }
    
    /**
     * Checks access based on @ProtocolAccess annotation.
     * 
     * @return Boolean.TRUE if allowed, Boolean.FALSE if denied, null if not determined
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
        
        // Check value() attribute (simple allow list)
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
            String schema = findSchemaValue(protocol, classAccess);
            if (schema != null) return schema;
        }
        
        // Check legacy annotations
        if (ProtocolNames.HTTP.equals(protocol)) {
            HttpResource httpResource = method.getAnnotation(HttpResource.class);
            if (httpResource != null) return httpResource.value();
        } else if (ProtocolNames.WEBSOCKET.equals(protocol)) {
            WebSocketResource wsResource = method.getAnnotation(WebSocketResource.class);
            if (wsResource != null) return wsResource.value();
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
    
    private boolean hasLegacyMapping(Method method, String protocol) {
        switch (protocol) {
            case ProtocolNames.HTTP:
                return method.getAnnotationsByType(HttpResource.class).length > 0;
            case ProtocolNames.WEBSOCKET:
                return method.getAnnotationsByType(WebSocketResource.class).length > 0;
            default:
                return false;
        }
    }
    
    private boolean hasProtocolMapping(Method method, String protocol) {
        ProtocolMapping[] mappings = method.getAnnotationsByType(ProtocolMapping.class);
        for (ProtocolMapping mapping : mappings) {
            if (mapping.protocol().equals(protocol)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasAnyProtocolMapping(Method method) {
        return method.getAnnotationsByType(ProtocolAccess.class).length > 0 ||
               method.getAnnotationsByType(ProtocolMapping.class).length > 0 ||
               method.getAnnotationsByType(HttpResource.class).length > 0 ||
               method.getAnnotationsByType(WebSocketResource.class).length > 0;
    }
}
