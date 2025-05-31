package horizon.core.scanner;

import horizon.core.ProtocolAggregator;
import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
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
import java.util.Objects;

/**
 * Scans for classes annotated with @Conductor and registers them with the ProtocolAggregator.
 */
public class ConductorScanner {
    private static final Logger logger = LoggerFactory.getLogger(ConductorScanner.class);

    /**
     * Scans the specified package for Conductor classes and registers them with the aggregator.
     * 
     * @return the list of ConductorMethod instances found
     */
    public List<ConductorMethod> scan(String basePackage, ProtocolAggregator aggregator) {
        logger.info("Scanning for conductors in package: {}", basePackage);

        List<ConductorMethod> allMethods = new ArrayList<>();

        try {
            List<Class<?>> classes = findClasses(basePackage);
            int conductorCount = 0;

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Conductor.class)) {
                    logger.debug("Found conductor class: {}", clazz.getName());
                    conductorCount++;

                    List<ConductorMethod> methods = processConductorClass(clazz);
                    allMethods.addAll(methods);
                }
            }

            // Register protocol-specific mappings
            registerProtocolMappings(allMethods, aggregator);

            logger.info("Found {} conductors with {} intent methods", conductorCount, allMethods.size());

        } catch (Exception e) {
            logger.error("Error scanning for conductors", e);
            throw new RuntimeException("Failed to scan for conductors", e);
        }

        return allMethods;
    }

    /**
     * Registers protocol mappings from conductor methods.
     */
    private void registerProtocolMappings(List<ConductorMethod> methods, ProtocolAggregator aggregator) {
        // Register HTTP mappings if an HTTP adapter is available
        registerHttpMappings(methods, aggregator);
        
        // Register gRPC mappings if a gRPC adapter is available
        registerGrpcMappings(methods, aggregator);
    }
    
    /**
     * Registers HTTP mappings for conductor methods.
     */
    private void registerHttpMappings(List<ConductorMethod> methods, ProtocolAggregator aggregator) {
        try {
            Object httpAdapter = aggregator.getProtocolAdapter(ProtocolNames.HTTP);
            if (httpAdapter != null) {
                // Check if the adapter is configurable
                if (adapterClass.getName().contains("ConfigurableHttpProtocolAdapter")) {
                    registerHttpMappingsViaReflection(methods, httpAdapter);
                }
            }
        } catch (Exception e) {
            logger.debug("Could not register HTTP mappings: {}", e.getMessage());
        }
    }
    
    private void registerHttpMappingsViaReflection(List<ConductorMethod> methods, Object httpAdapter) throws Exception {
        Class<?> resolverClass = Class.forName("horizon.web.http.resolver.AnnotationBasedHttpIntentResolver");
        Object annotationResolver = resolverClass.getDeclaredConstructor().newInstance();

        Method registerMethod = resolverClass.getMethod("registerConductorMethod", ConductorMethod.class);
        for (ConductorMethod method : methods) {
            registerMethod.invoke(annotationResolver, method);
        }

        Method addResolverMethod = httpAdapter.getClass().getMethod("addResolver", 
            Class.forName("horizon.core.protocol.IntentResolver"));
        addResolverMethod.invoke(httpAdapter, annotationResolver);

        logger.debug("Registered {} HTTP mappings", methods.size());
    }

    /**
     * Registers gRPC mappings for conductor methods.
     */
    private void registerGrpcMappings(List<ConductorMethod> methods, ProtocolAggregator aggregator) {
        try {
            Object grpcAdapter = aggregator.getProtocolAdapter(ProtocolNames.GRPC);
            if (grpcAdapter != null) {
                // Access GrpcServiceRegistry
                Class<?> registryClass = Class.forName("horizon.web.grpc.GrpcServiceRegistry");
                Method getInstanceMethod = registryClass.getMethod("getInstance");
                Object registry = getInstanceMethod.invoke(null);
                
                // Register methods that have gRPC protocol access
                for (ConductorMethod conductorMethod : methods) {
                    Method method = conductorMethod.getMethod();
                    
                    // Check for @ProtocolAccess with gRPC schema
                    horizon.core.annotation.ProtocolAccess protocolAccess = method.getAnnotation(horizon.core.annotation.ProtocolAccess.class);
                    if (protocolAccess == null) {
                        protocolAccess = method.getDeclaringClass().getAnnotation(horizon.core.annotation.ProtocolAccess.class);
                    }
                    
                    if (protocolAccess != null) {
                        for (horizon.core.annotation.ProtocolSchema schema : protocolAccess.schema()) {
                            if (ProtocolNames.GRPC.equals(schema.protocol()) && !schema.value().isEmpty()) {
                                // Parse service/method from schema value
                                String[] parts = schema.value().split("/");
                                if (parts.length == 2) {
                                    String serviceName = parts[0];
                                    String methodName = parts[1];
                                    
                                    // Register in GrpcServiceRegistry
                                    Method registerMethod = registryClass.getMethod("registerMethod", 
                                        ConductorMethod.class, String.class, String.class);
                                    registerMethod.invoke(registry, conductorMethod, serviceName, methodName);
                                    
                                    logger.debug("Registered gRPC mapping: {} -> {}", 
                                        schema.value(), conductorMethod.getIntent());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not register gRPC mappings: {}", e.getMessage());
        }
    }

    /**
     * Processes a single Conductor class and extracts its intent methods.
     */
    private List<ConductorMethod> processConductorClass(Class<?> clazz) {
        List<ConductorMethod> methods = new ArrayList<>();

        Conductor conductorAnnotation = clazz.getAnnotation(Conductor.class);
        String namespace = Objects.requireNonNull(conductorAnnotation).namespace();

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
            // TODO: Add support for JAR files when needed
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
}
