package horizon.core.conductor;

import horizon.core.annotation.ProtocolAccess;
import horizon.core.annotation.ProtocolSchema;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Metadata about a conductor method including protocol mappings and parameter information.
 * This class provides a cleaner way to access conductor configuration.
 */
public class ConductorMetadata {
    private final String intent;
    private final ConductorMethod conductorMethod;
    private final Map<String, ProtocolMapping> protocolMappings = new HashMap<>();
    private final List<ParameterMetadata> parameters = new ArrayList<>();
    
    public ConductorMetadata(ConductorMethod conductorMethod) {
        this.conductorMethod = conductorMethod;
        this.intent = conductorMethod.getIntent();
        analyzeMetadata();
    }
    
    private void analyzeMetadata() {
        Method method = conductorMethod.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        
        // Analyze protocol access
        ProtocolAccess methodAccess = method.getAnnotation(ProtocolAccess.class);
        ProtocolAccess classAccess = declaringClass.getAnnotation(ProtocolAccess.class);
        
        // Method-level takes precedence
        if (methodAccess != null) {
            processProtocolAccess(methodAccess);
        } else if (classAccess != null) {
            processProtocolAccess(classAccess);
        }
        
        // Analyze parameters
        conductorMethod.getParameters().forEach(param -> {
            parameters.add(new ParameterMetadata(
                param.getName(),
                param.getType(),
                param.isRequired(),
                param.getDefaultValue(),
                param.getHints()
            ));
        });
    }
    
    private void processProtocolAccess(ProtocolAccess access) {
        // Process schema definitions
        for (ProtocolSchema schema : access.schema()) {
            protocolMappings.put(schema.protocol(), new ProtocolMapping(
                schema.protocol(),
                schema.value(),
                parseAttributes(schema.attributes())
            ));
        }
        
        // Process simple access list
        if (access.value().length > 0 && access.schema().length == 0) {
            for (String protocol : access.value()) {
                protocolMappings.put(protocol, new ProtocolMapping(protocol, "", Map.of()));
            }
        }
    }
    
    private Map<String, String> parseAttributes(String[] attributes) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < attributes.length - 1; i += 2) {
            result.put(attributes[i], attributes[i + 1]);
        }
        return result;
    }
    
    // Getters
    public String getIntent() { return intent; }
    public ConductorMethod getConductorMethod() { return conductorMethod; }
    public Map<String, ProtocolMapping> getProtocolMappings() { return protocolMappings; }
    public List<ParameterMetadata> getParameters() { return parameters; }
    
    public boolean supportsProtocol(String protocol) {
        return protocolMappings.containsKey(protocol);
    }
    
    public ProtocolMapping getProtocolMapping(String protocol) {
        return protocolMappings.get(protocol);
    }
    
    /**
     * Protocol-specific mapping information.
     */
    public static class ProtocolMapping {
        private final String protocol;
        private final String schema;
        private final Map<String, String> attributes;
        
        public ProtocolMapping(String protocol, String schema, Map<String, String> attributes) {
            this.protocol = protocol;
            this.schema = schema;
            this.attributes = attributes;
        }
        
        public String getProtocol() { return protocol; }
        public String getSchema() { return schema; }
        public Map<String, String> getAttributes() { return attributes; }
        public String getAttribute(String key) { return attributes.get(key); }
    }
    
    /**
     * Parameter metadata.
     */
    public static class ParameterMetadata {
        private final String name;
        private final Class<?> type;
        private final boolean required;
        private final String defaultValue;
        private final String[] hints;
        
        public ParameterMetadata(String name, Class<?> type, boolean required, 
                                String defaultValue, String[] hints) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
            this.hints = hints;
        }
        
        public String getName() { return name; }
        public Class<?> getType() { return type; }
        public boolean isRequired() { return required; }
        public String getDefaultValue() { return defaultValue; }
        public String[] getHints() { return hints; }
    }
}
