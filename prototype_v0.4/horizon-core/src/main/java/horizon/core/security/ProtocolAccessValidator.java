package horizon.core.security;

import horizon.core.annotation.*;
import horizon.core.conductor.ConductorMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates protocol access to conductors based on protocol mappings and access annotations.
 * <p>
 * Access is granted if:
 * 1. The method has a @ProtocolMapping for protocol
 * 2. The method has an @HttpResource or @WebSocketResource annotation for protocol
 * 3. The method/class has @ProtocolAccess that includes protocol
 * 4. No access restrictions are defined (backward compatibility)
 */
public class ProtocolAccessValidator {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolAccessValidator.class);
    
    /**
     * Checks if a protocol has access to a conductor method.
     */
    public boolean hasAccess(String protocol, ConductorMethod conductorMethod) {
        Method method = conductorMethod.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        
        // First, check if the method has protocol mapping - this automatically grants access
        if (hasProtocolMapping(method, protocol)) {
            logger.debug("Access granted to {} for {} via @ProtocolMapping", protocol, method.getName());
            return true;
        }
        
        // Check convenience annotations
        if (hasConvenienceMapping(method, protocol)) {
            logger.debug("Access granted to {} for {} via convenience annotation", protocol, method.getName());
            return true;
        }
        
        // Check explicit @ProtocolAccess on method
        ProtocolAccess methodAccess = method.getAnnotation(ProtocolAccess.class);
        if (methodAccess != null) {
            return checkAccess(protocol, methodAccess);
        }
        
        // Check explicit @ProtocolAccess on class
        ProtocolAccess classAccess = declaringClass.getAnnotation(ProtocolAccess.class);
        if (classAccess != null) {
            return checkAccess(protocol, classAccess);
        }
        
        // No access control defined - check if any protocol mapping exists
        boolean hasAnyMapping = method.getAnnotationsByType(ProtocolMapping.class).length > 0 ||
                               method.getAnnotationsByType(HttpResource.class).length > 0 ||
                               method.getAnnotationsByType(WebSocketResource.class).length > 0;
        
        if (hasAnyMapping) {
            // Has mappings but not for this protocol - deny access
            logger.debug("Access denied to {} for {} - has mappings but not for this protocol", 
                        protocol, method.getName());
            return false;
        }
        
        // No mappings at all - allow for backward compatibility
        logger.warn("No protocol mappings found for {}.{} - allowing all protocols (backward compatibility)", 
                   declaringClass.getSimpleName(), method.getName());
        return true;
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
    
    private boolean hasConvenienceMapping(Method method, String protocol) {
        switch (protocol) {
            case "HTTP":
                return method.getAnnotationsByType(HttpResource.class).length > 0;
            case "WebSocket":
                return method.getAnnotationsByType(WebSocketResource.class).length > 0;
            default:
                return false;
        }
    }
    
    private boolean checkAccess(String protocol, ProtocolAccess access) {
        Set<String> allowedProtocols = new HashSet<>(Arrays.asList(access.value()));
        
        if (allowedProtocols.contains(protocol)) {
            return true;
        }
        
        return access.allowOthers();
    }
}
