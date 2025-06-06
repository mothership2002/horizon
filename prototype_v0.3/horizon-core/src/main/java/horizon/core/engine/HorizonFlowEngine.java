package horizon.core.engine;

import horizon.core.annotation.Intent;
import horizon.core.command.Command;
import horizon.core.conductor.Conductor;
import horizon.core.constant.Scheme;
import horizon.core.context.HorizonRuntimeUnit;
import horizon.core.context.HorizonSystemContext;
import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.Rendezvous;
import horizon.core.stage.StageHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HorizonFlowEngine orchestrates the entire flow through a single entry point (run):
 * • RawInput → HorizonContext creation (encounter)
 * • Intent → Conductor → Command → Execution
 * • StageHandler → RawOutput conversion
 * • Rendezvous.fallAway(context) call
 */
public class HorizonFlowEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(HorizonFlowEngine.class);

    private final HorizonSystemContext systemContext;
    private final PerformanceMonitor performanceMonitor;
    private final Map<String, Map<String, Method>> intentMethodCache = new HashMap<>();

    /**
     * Creates a new HorizonFlowEngine with the given system context.
     *
     * @param systemContext the system context containing registered runtime units
     * @throws NullPointerException if systemContext is null
     */
    public HorizonFlowEngine(HorizonSystemContext systemContext) {
        this.systemContext = Objects.requireNonNull(systemContext, "systemContext must not be null");
        this.performanceMonitor = new PerformanceMonitor();
    }

    /**
     * Runs the flow for the given input and returns the output.
     * This method is synchronous and will block until the flow completes.
     *
     * @param input the raw input to process
     * @return the raw output produced by the flow
     * @throws IllegalArgumentException if the input is invalid
     * @throws IllegalStateException if no runtime unit is found for the input's scheme,
     *                               no conductor is found for the parsed intent,
     *                               or no stage handler is found for the command's key
     * @throws NullPointerException if the input is null
     */
    public RawOutput run(RawInput input) {
        Objects.requireNonNull(input, "input must not be null");
        LOGGER.info("Starting flow for input from source: {}", input.getSource());

        long startTime = System.currentTimeMillis();
        try {
            // Resolve runtime unit
            HorizonRuntimeUnit<?, ?, ?, ?, ?> unit = resolveRuntimeUnit(input);

            // Create context
            HorizonContext context = createContext(input, unit);

            // Execute command
            context = executeCommand(context, unit);

            // Handle result
            RawOutput output = handleResult(context, unit);

            // Finalize output
            return finalizeOutput(context, unit, output);
        } catch (Exception e) {
            LOGGER.error("Error processing input: {}", e.getMessage(), e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            performanceMonitor.recordExecution(input.getScheme(), endTime - startTime);
            LOGGER.info("Completed flow for input from source: " + input.getSource() + 
                    " in " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    /**
     * Runs the flow for the given input asynchronously and returns a CompletableFuture
     * that will be completed with the output when the flow completes.
     *
     * @param input the raw input to process
     * @return a CompletableFuture that will be completed with the raw output
     * @throws NullPointerException if the input is null
     */
    public CompletableFuture<RawOutput> runAsync(RawInput input) {
        Objects.requireNonNull(input, "input must not be null");
        return CompletableFuture.supplyAsync(() -> run(input));
    }

    /**
     * Resolves the runtime unit for the given input.
     *
     * @param input the raw input to process
     * @return the resolved runtime unit
     * @throws IllegalStateException if no runtime unit is found for the input's scheme
     */
    private HorizonRuntimeUnit<?, ?, ?, ?, ?> resolveRuntimeUnit(RawInput input) {
        LOGGER.info("Resolving runtime unit for scheme: " + input.getScheme());

        Optional<HorizonRuntimeUnit<RawInput, Object, Object, Object, RawOutput>> optUnit
                = systemContext.resolveUnit(Scheme.valueOf(input.getScheme()));

        return optUnit.orElseThrow(() -> {
            String message = "No runtime for scheme " + input.getScheme();
            LOGGER.error(message);
            return new IllegalStateException(message);
        });
    }

    /**
     * Creates a context for the given input using the specified runtime unit.
     *
     * @param input the raw input to process
     * @param unit the runtime unit to use
     * @return the created context
     */
    private HorizonContext createContext(RawInput input, HorizonRuntimeUnit<?, ?, ?, ?, ?> unit) {
        LOGGER.info("Creating context for input from source: {}", input.getSource());

        @SuppressWarnings("unchecked")
        Rendezvous<RawInput, RawOutput> rendezvous =
                (Rendezvous<RawInput, RawOutput>) unit.getRendezvousDescriptor().rendezvous();

        try {
            return rendezvous.encounter(input);
        } catch (Exception e) {
            LOGGER.error("Error creating context: {}", e.getMessage(), e);
            return rendezvous.handleError(e, input);
        }
    }

    /**
     * Executes the command for the given context using the specified runtime unit.
     *
     * @param context the context to process
     * @param unit the runtime unit to use
     * @return the updated context
     * @throws IllegalStateException if no conductor is found for the parsed intent
     */
    private HorizonContext executeCommand(HorizonContext context, HorizonRuntimeUnit<?, ?, ?, ?, ?> unit) {
        LOGGER.info("Executing command for intent: {}", context.getParsedIntent());

        if (context.hasFailed()) {
            LOGGER.warn("Context has failed, skipping command execution");
            return context;
        }

        try {
            final String parsedIntent = context.getParsedIntent();
            @SuppressWarnings("unchecked")
            Conductor<Object> conductor =
                    (Conductor<Object>) unit.getConductor(parsedIntent)
                            .orElseThrow(() -> {
                                String message = "No conductor for intent " + parsedIntent;
                                LOGGER.error(message);
                                return new IllegalStateException(message);
                            });

            // Try to find a method annotated with @Intent that matches the parsed intent
            Command<?> command = findAndInvokeIntentMethod(conductor, parsedIntent, context.getIntentPayload());

            // If no matching method is found, fall back to the resolve method
            if (command == null) {
                LOGGER.debug("No @Intent method found for intent {}, falling back to resolve method", parsedIntent);
                command = conductor.resolve(context.getIntentPayload());
            }

            Object result = command.execute();
            context.setExecutionResult(result);

            return context;
        } catch (Exception e) {
            LOGGER.error("Error executing command: {}", e.getMessage(), e);
            context.setFailureCause(e);
            return context;
        }
    }

    /**
     * Finds a method annotated with @Intent that matches the given intent name and invokes it.
     *
     * @param conductor the conductor instance
     * @param intentName the intent name to match
     * @param payload the payload to pass to the method
     * @return the command returned by the method, or null if no matching method is found
     */
    private Command<?> findAndInvokeIntentMethod(Conductor<?> conductor, String intentName, Object payload) {
        Class<?> conductorClass = conductor.getClass();
        String className = conductorClass.getName();

        // Check if the class is annotated with @Conductor
        horizon.core.annotation.Conductor conductorAnnotation = 
                conductorClass.getAnnotation(horizon.core.annotation.Conductor.class);

        if (conductorAnnotation == null) {
            LOGGER.debug("Conductor class {} is not annotated with @Conductor", className);
            return null;
        }

        // Get the namespace from the @Conductor annotation
        String namespace = conductorAnnotation.namespace();

        // If the intent name starts with the namespace, remove it
        String methodIntentName = intentName;
        if (!namespace.isEmpty() && intentName.startsWith(namespace)) {
            methodIntentName = intentName.substring(namespace.length());
            // Remove leading dot or slash if present
            if (methodIntentName.startsWith(".") || methodIntentName.startsWith("/")) {
                methodIntentName = methodIntentName.substring(1);
            }
        }

        // Check if we have cached the methods for this class
        Map<String, Method> methodMap = intentMethodCache.get(className);
        if (methodMap == null) {
            // Cache the methods for this class
            methodMap = new HashMap<>();
            intentMethodCache.put(className, methodMap);

            // Scan the class for methods annotated with @Intent
            for (Method method : conductorClass.getMethods()) {
                Intent intentAnnotation = method.getAnnotation(Intent.class);
                if (intentAnnotation == null) {
                    continue;
                }

                // Get the intent name from the annotation
                String annotationValue = intentAnnotation.value();
                if (annotationValue.isEmpty()) {
                    annotationValue = intentAnnotation.name();
                }

                methodMap.put(annotationValue, method);
            }
        }

        // Look for a method that matches the intent name
        Method method = methodMap.get(methodIntentName);
        if (method != null) {
            try {
                // Invoke the method with the payload
                if (method.getParameterCount() == 0) {
                    return (Command<?>) method.invoke(conductor);
                } else {
                    return (Command<?>) method.invoke(conductor, payload);
                }
            } catch (Exception e) {
                LOGGER.error("Error invoking @Intent method: {}", e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * Handles the result for the given context using the specified runtime unit.
     *
     * @param context the context to process
     * @param unit the runtime unit to use
     * @return the raw output produced by handling the result
     * @throws IllegalStateException if no stage handler is found for the command's key
     */
    private RawOutput handleResult(HorizonContext context, HorizonRuntimeUnit<?, ?, ?, ?, ?> unit) {
        LOGGER.info("Handling result for context: {}", context.getTraceId());

        if (context.hasFailed()) {
            LOGGER.warn("Context has failed, creating error response");
            // Create a default error response
            return new ErrorOutput(context.getFailureCause());
        }

        try {
            final String commandKey;
            if (context.getIntentPayload() instanceof Command) {
                commandKey = ((Command<?>) context.getIntentPayload()).getKey();
            } else {
                // Fallback to intent if command key is not available
                commandKey = context.getParsedIntent();
            }

            StageHandler handler = unit.getCentralStage(commandKey)
                    .orElseThrow(() -> {
                        String message = "No stage handler for command " + commandKey;
                        LOGGER.error(message);
                        return new IllegalStateException(message);
                    });

            RawOutput output = handler.handle(context);
            context.setRenderedOutput(output);

            return output;
        } catch (Exception e) {
            LOGGER.error("Error handling result: {}", e.getMessage(), e);
            context.setFailureCause(e);
            return new ErrorOutput(e);
        }
    }

    /**
     * Finalizes the output for the given context.
     *
     * @param context the context to process
     * @param unit the runtime unit to use
     * @param output the raw output produced by handling the result
     * @return the finalized raw output
     */
    private RawOutput finalizeOutput(HorizonContext context, HorizonRuntimeUnit<?, ?, ?, ?, ?> unit, RawOutput output) {
        LOGGER.info("Finalizing output for context: {}", context.getTraceId());

        try {
            @SuppressWarnings("unchecked")
            Rendezvous<RawInput, RawOutput> rendezvous =
                    (Rendezvous<RawInput, RawOutput>) unit.getRendezvousDescriptor().rendezvous();

            RawOutput finalOutput = rendezvous.fallAway(context);
            return finalOutput != null ? finalOutput : output;
        } catch (Exception e) {
            LOGGER.error("Error finalizing output: {}", e.getMessage(), e);
            return output; // Return the original output if finalization fails
        }
    }

    /**
     * Returns the performance monitor for this flow engine.
     *
     * @return the performance monitor
     */
    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    /**
     * A simple implementation of RawOutput for error responses.
     */
    private static class ErrorOutput implements RawOutput {
        private final Throwable cause;

        public ErrorOutput(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public Object getContent() {
            return cause.getMessage();
        }

        @Override
        public int getStatusCode() {
            return 500; // Internal Server Error
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public Object getMetadata() {
            return cause;
        }
    }

    /**
     * A simple performance monitor for tracking execution times.
     */
    public static class PerformanceMonitor {
        private final java.util.Map<String, java.util.List<Long>> executionTimes = new java.util.concurrent.ConcurrentHashMap<>();

        /**
         * Records an execution time for the specified scheme.
         *
         * @param scheme the scheme of the execution
         * @param timeMs the execution time in milliseconds
         */
        public void recordExecution(String scheme, long timeMs) {
            executionTimes.computeIfAbsent(scheme, k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                    .add(timeMs);
        }

        /**
         * Returns the average execution time for the specified scheme.
         *
         * @param scheme the scheme to get the average for
         * @return the average execution time in milliseconds, or 0 if no executions have been recorded
         */
        public double getAverageExecutionTime(String scheme) {
            java.util.List<Long> times = executionTimes.get(scheme);
            if (times == null || times.isEmpty()) {
                return 0;
            }

            return times.stream().mapToLong(Long::longValue).average().orElse(0);
        }

        /**
         * Returns the total number of executions for the specified scheme.
         *
         * @param scheme the scheme to get the count for
         * @return the total number of executions
         */
        public int getExecutionCount(String scheme) {
            java.util.List<Long> times = executionTimes.get(scheme);
            return times == null ? 0 : times.size();
        }

        /**
         * Returns a map of all execution times grouped by scheme.
         *
         * @return a map of execution times
         */
        public java.util.Map<String, java.util.List<Long>> getAllExecutionTimes() {
            return new java.util.HashMap<>(executionTimes);
        }
    }
}
