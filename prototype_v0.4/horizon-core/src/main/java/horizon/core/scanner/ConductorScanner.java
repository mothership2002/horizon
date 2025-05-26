package horizon.core.scanner;

import horizon.core.ProtocolAggregator;
import horizon.core.annotation.Conductor;
import horizon.core.annotation.Intent;
import horizon.core.conductor.ConductorMethod;
import horizon.core.protocol.ProtocolNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

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
        try {
            Object httpAdapter = aggregator.getProtocolAdapter(ProtocolNames.HTTP);
            if (httpAdapter != null) {
                // Check if the adapter is a ConfigurableHttpProtocolAdapter using reflection
                Class<?> adapterClass = httpAdapter.getClass();

                // Check if the class or any of its interfaces/superclasses has the expected name
                boolean isConfigurable = false;
                for (Class<?> iface : adapterClass.getInterfaces()) {
                    if (iface.getName().equals("horizon.web.http.ConfigurableHttpProtocolAdapter")) {
                        isConfigurable = true;
                        break;
                    }
                }

                if (!isConfigurable && adapterClass.getSuperclass() != null) {
                    Class<?> superClass = adapterClass.getSuperclass();
                    if (superClass.getName().equals("horizon.web.http.ConfigurableHttpProtocolAdapter")) {
                        isConfigurable = true;
                    }
                }

                if (isConfigurable) {
                    // Create an annotation-based resolver using reflection
                    Class<?> resolverClass = Class.forName("horizon.web.http.resolver.AnnotationBasedHttpIntentResolver");
                    Object annotationResolver = resolverClass.getDeclaredConstructor().newInstance();

                    // Register conductor methods
                    Method registerMethod = resolverClass.getMethod("registerConductorMethod", ConductorMethod.class);
                    for (ConductorMethod method : methods) {
                        registerMethod.invoke(annotationResolver, method);
                    }

                    // Add resolver to adapter
                    Method addResolverMethod = adapterClass.getMethod("addResolver", resolverClass);
                    addResolverMethod.invoke(httpAdapter, annotationResolver);

                    logger.debug("Registered {} HTTP mappings", methods.size());
                }
            }
        } catch (Exception e) {
            logger.debug("Could not register HTTP mappings: {}", e.getMessage());
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

        public ConductorMethodAdapter(ConductorMethod method) {
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
