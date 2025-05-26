package horizon.core.scanner;

import horizon.core.ProtocolAggregator;
import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.annotation.ProtocolAccess;
import horizon.core.annotation.ProtocolSchema;
import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Scans for classes annotated with @Conductor and registers them with the ProtocolAggregator.
 */
public class ConductorScanner {
    private static final Logger logger = LoggerFactory.getLogger(ConductorScanner.class);
    
    /**
     * Scans the specified package for Conductor classes and registers them with the aggregator.
     */
    public void scan(String basePackage, ProtocolAggregator aggregator) {
        logger.info("Scanning for conductors in package: {}", basePackage);
        
        try {
            List<Class<?>> classes = findClasses(basePackage);
            int conductorCount = 0;
            int methodCount = 0;
            
            // Collect all conductor methods first
            List<ConductorMethod> allMethods = new ArrayList<>();
            
            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Conductor.class)) {
                    logger.debug("Found conductor class: {}", clazz.getName());
                    conductorCount++;
                    
                    List<ConductorMethod> methods = processConductorClass(clazz);
                    allMethods.addAll(methods);
                    methodCount += methods.size();
                }
            }
            
            // Register protocol mappings (if HTTP protocol is registered)
            registerProtocolMappings(allMethods, aggregator);
            
            // Register each method as a conductor
            for (ConductorMethod method : allMethods) {
                aggregator.registerConductor(new ConductorMethodAdapter(method));
            }
            
            logger.info("Registered {} conductors with {} intent methods", conductorCount, methodCount);
            
        } catch (Exception e) {
            logger.error("Error scanning for conductors", e);
            throw new RuntimeException("Failed to scan for conductors", e);
        }
    }
    
    /**
     * Registers protocol mappings from conductor methods.
     */
    private void registerProtocolMappings(List<ConductorMethod> methods, ProtocolAggregator aggregator) {
        // This is a bit of a hack to get the HTTP adapter
        // In a real implementation, we'd have a better way to access protocol adapters
        try {
            // Use reflection to access the protocol adapters
            // For now, we'll skip this as it would require changes to ProtocolAggregator API
            logger.debug("Protocol mapping registration would happen here");
        } catch (Exception e) {
            logger.debug("Could not register protocol mappings: {}", e.getMessage());
        }
    }
    
    /**
     * Processes a single Conductor class and extracts its intent methods.
     */
    private List<ConductorMethod> processConductorClass(Class<?> clazz) {
        List<ConductorMethod> methods = new ArrayList<>();
        
        Conductor conductorAnnotation = clazz.getAnnotation(Conductor.class);
        String namespace = conductorAnnotation.namespace();
        
        // Create instance
        Object instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Failed to instantiate conductor: {}", clazz.getName(), e);
            return methods;
        }
        
        // Process methods
        for (Method method : clazz.getDeclaredMethods()) {
            Intent intentAnnotation = method.getAnnotation(Intent.class);
            if (intentAnnotation != null) {
                // Build full intent name
                String intentName = intentAnnotation.value();
                String fullIntent = namespace.isEmpty() ? intentName : namespace + "." + intentName;
                
                logger.debug("Registering intent method: {} -> {}#{}", 
                    fullIntent, clazz.getSimpleName(), method.getName());
                
                methods.add(new ConductorMethod(instance, method, fullIntent));
                
                // Also register aliases
                for (String alias : intentAnnotation.aliases()) {
                    String fullAlias = namespace.isEmpty() ? alias : namespace + "." + alias;
                    methods.add(new ConductorMethod(instance, method, fullAlias));
                }
            }
        }
        
        return methods;
    }
    
    /**
     * Finds all classes in the specified package.
     */
    private List<Class<?>> findClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.toURI());
                if (directory.exists()) {
                    findClassesInDirectory(directory, packageName, classes);
                }
            }
        }
        
        return classes;
    }
    
    /**
     * Recursively finds classes in a directory.
     */
    private void findClassesInDirectory(File directory, String packageName, List<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    logger.warn("Failed to load class: {}", className);
                }
            }
        }
    }
    
    /**
     * Adapter that wraps a ConductorMethod to implement the Conductor interface.
     */
    public static class ConductorMethodAdapter implements horizon.core.Conductor<Object, Object> {
        public final ConductorMethod method;  // public for access validation
        
        ConductorMethodAdapter(ConductorMethod method) {
            this.method = method;
        }
        
        @Override
        public Object conduct(Object payload) {
            try {
                return method.invoke(payload);
            } catch (Exception e) {
                if (e.getCause() != null) {
                    // Unwrap the reflection exception
                    throw new RuntimeException(e.getCause());
                }
                throw new RuntimeException("Failed to invoke conductor method", e);
            }
        }
        
        @Override
        public String getIntentPattern() {
            return method.getIntent();
        }
    }
}
