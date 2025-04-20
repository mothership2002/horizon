package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract implementation of the Rendezvous interface that provides
 * common functionality for all rendezvous implementations.
 * 
 * This class handles the flow of data through sentinels, normalizers, and interpreters,
 * and provides hooks for subclasses to customize the behavior at various points.
 *
 * @param <I> the type of raw input this rendezvous can handle
 * @param <N> the type of normalized input
 * @param <K> the type of intent key
 * @param <P> the type of intent payload
 * @param <O> the type of raw output this rendezvous produces
 */
public abstract class AbstractRendezvous<I extends RawInput, N, K, P, O extends RawOutput> implements Rendezvous<I, O> {
    private static final Logger LOGGER = Logger.getLogger(AbstractRendezvous.class.getName());

    protected final List<Sentinel<I>> sentinels;
    protected final Normalizer<I, N> normalizer;
    protected final Interpreter<N, K, P> interpreter;
    private final ExecutorService executorService;
    private final boolean parallelSentinelProcessing;

    /**
     * Creates a new AbstractRendezvous with the specified sentinels, normalizer, and interpreter.
     * Sentinel processing will be performed sequentially.
     *
     * @param sentinels the sentinels to use for inspecting input and output
     * @param normalizer the normalizer to use for normalizing input
     * @param interpreter the interpreter to use for extracting intent key and payload
     * @throws NullPointerException if sentinels, normalizer, or interpreter is null
     */
    public AbstractRendezvous(List<Sentinel<I>> sentinels, Normalizer<I, N> normalizer, Interpreter<N, K, P> interpreter) {
        this(sentinels, normalizer, interpreter, false);
    }

    /**
     * Creates a new AbstractRendezvous with the specified sentinels, normalizer, interpreter,
     * and parallel sentinel processing flag.
     *
     * @param sentinels the sentinels to use for inspecting input and output
     * @param normalizer the normalizer to use for normalizing input
     * @param interpreter the interpreter to use for extracting intent key and payload
     * @param parallelSentinelProcessing whether to process sentinels in parallel
     * @throws NullPointerException if sentinels, normalizer, or interpreter is null
     */
    public AbstractRendezvous(List<Sentinel<I>> sentinels, Normalizer<I, N> normalizer, Interpreter<N, K, P> interpreter, boolean parallelSentinelProcessing) {
        this.sentinels = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(sentinels, "sentinels")));
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter");
        this.parallelSentinelProcessing = parallelSentinelProcessing;
        this.executorService = parallelSentinelProcessing ? Executors.newFixedThreadPool(
                Math.min(sentinels.size(), Runtime.getRuntime().availableProcessors())) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HorizonContext encounter(I input) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(input, "input must not be null");
        LOGGER.fine("Encountering input from source: " + input.getSource());

        try {
            // Process input through sentinels
            processSentinelsInbound(input);

            // Normalize input
            N normalized = normalizeInput(input);

            // Extract intent key and payload
            K key = extractIntentKey(normalized);
            P payload = extractIntentPayload(normalized);

            // Create and populate context
            HorizonContext context = createContext(input, key, payload);

            // Allow subclasses to customize the context
            return customizeContext(context);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error encountering input: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public O fallAway(HorizonContext context) throws IllegalArgumentException, NullPointerException {
        Objects.requireNonNull(context, "context must not be null");
        LOGGER.fine("Falling away context: " + context.getTraceId());

        try {
            // Process context through sentinels
            processSentinelsOutbound(context);

            // Get rendered output
            O output = getRenderedOutput(context);

            // Allow subclasses to customize the output
            return customizeOutput(output, context);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error falling away context: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HorizonContext handleError(Exception e, I input) {
        Objects.requireNonNull(input, "input must not be null");
        LOGGER.log(Level.WARNING, "Handling error: " + e.getMessage(), e);

        HorizonContext context = new HorizonContext(input);
        context.setFailureCause(e);
        return context;
    }

    /**
     * Processes the input through all sentinels.
     * If parallel sentinel processing is enabled, sentinels will be processed in parallel.
     *
     * @param input the input to process
     */
    protected void processSentinelsInbound(I input) {
        LOGGER.fine("Processing input through sentinels");

        if (parallelSentinelProcessing && sentinels.size() > 1) {
            // Process sentinels in parallel
            List<CompletableFuture<Void>> futures = new ArrayList<>(sentinels.size());
            for (Sentinel<I> sentinel : sentinels) {
                futures.add(CompletableFuture.runAsync(() -> sentinel.inspectInbound(input), executorService));
            }

            // Wait for all sentinels to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } else {
            // Process sentinels sequentially
            for (Sentinel<I> sentinel : sentinels) {
                sentinel.inspectInbound(input);
            }
        }
    }

    /**
     * Processes the context through all sentinels.
     * If parallel sentinel processing is enabled, sentinels will be processed in parallel.
     *
     * @param context the context to process
     */
    protected void processSentinelsOutbound(HorizonContext context) {
        LOGGER.fine("Processing context through sentinels");

        if (parallelSentinelProcessing && sentinels.size() > 1) {
            // Process sentinels in parallel
            List<CompletableFuture<Void>> futures = new ArrayList<>(sentinels.size());
            for (Sentinel<I> sentinel : sentinels) {
                futures.add(CompletableFuture.runAsync(() -> sentinel.inspectOutbound(context), executorService));
            }

            // Wait for all sentinels to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } else {
            // Process sentinels sequentially
            for (Sentinel<I> sentinel : sentinels) {
                sentinel.inspectOutbound(context);
            }
        }
    }

    /**
     * Normalizes the input using the normalizer.
     * This method can be overridden by subclasses to customize the normalization process.
     *
     * @param input the input to normalize
     * @return the normalized input
     */
    protected N normalizeInput(I input) {
        LOGGER.fine("Normalizing input");
        return normalizer.normalize(input);
    }

    /**
     * Extracts the intent key from the normalized input using the interpreter.
     * This method can be overridden by subclasses to customize the key extraction process.
     *
     * @param normalized the normalized input
     * @return the intent key
     */
    protected K extractIntentKey(N normalized) {
        LOGGER.fine("Extracting intent key");
        return interpreter.extractIntentKey(normalized);
    }

    /**
     * Extracts the intent payload from the normalized input using the interpreter.
     * This method can be overridden by subclasses to customize the payload extraction process.
     *
     * @param normalized the normalized input
     * @return the intent payload
     */
    protected P extractIntentPayload(N normalized) {
        LOGGER.fine("Extracting intent payload");
        return interpreter.extractIntentPayload(normalized);
    }

    /**
     * Creates a context from the input, key, and payload.
     * This method can be overridden by subclasses to customize the context creation process.
     *
     * @param input the input
     * @param key the intent key
     * @param payload the intent payload
     * @return the created context
     */
    protected HorizonContext createContext(I input, K key, P payload) {
        LOGGER.fine("Creating context");

        HorizonContext context = new HorizonContext(input);
        context.setParsedIntent(key != null ? key.toString() : null);
        context.setIntentPayload(payload);
        return context;
    }

    /**
     * Customizes the context before returning it from the encounter method.
     * This method is a hook for subclasses to customize the context.
     * The default implementation returns the context unchanged.
     *
     * @param context the context to customize
     * @return the customized context
     */
    protected HorizonContext customizeContext(HorizonContext context) {
        // Hook for subclasses
        return context;
    }

    /**
     * Gets the rendered output from the context.
     * This method can be overridden by subclasses to customize the output retrieval process.
     *
     * @param context the context
     * @return the rendered output
     * @throws NullPointerException if no rendered output is set in the context
     */
    @SuppressWarnings("unchecked")
    protected O getRenderedOutput(HorizonContext context) {
        LOGGER.fine("Getting rendered output");

        return Objects.requireNonNull(
                (O) context.getRenderedOutput(),
                "No renderedOutput set in HorizonContext"
        );
    }

    /**
     * Customizes the output before returning it from the fallAway method.
     * This method is a hook for subclasses to customize the output.
     * The default implementation returns the output unchanged.
     *
     * @param output the output to customize
     * @param context the context
     * @return the customized output
     */
    protected O customizeOutput(O output, HorizonContext context) {
        // Hook for subclasses
        return output;
    }

    /**
     * Closes this rendezvous and releases any resources associated with it.
     * This method should be called when the rendezvous is no longer needed.
     */
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
