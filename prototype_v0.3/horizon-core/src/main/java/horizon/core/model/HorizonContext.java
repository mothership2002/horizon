package horizon.core.model;

import java.util.Objects;
import java.util.UUID;

/**
 * HorizonContext is the runtime flow container representing a single intent-driven execution unit.
 * It flows through the Horizon architecture: Rendezvous → Conductor → Stage.
 *
 * Note: This is NOT the system-wide component registry. For that, see HorizonSystemContext.
 */
public class HorizonContext {

    private final String traceId;              // Unique identifier for this flow
    private final String scheme;               // e.g., http, cli, ws
    private final String source;               // source of the request
    private final RawInput rawInput;           // raw input, protocol-agnostic abstraction

    private String parsedIntent;               // interpreted meaning
    private Object intentPayload;              // structured payload for downstream

    // Represents the outcome of intent realization within the system.
    // This is NOT necessarily the final response to the user.
    private Object executionResult;

    // Optional: The system's finalized rendering of the result, for external output.
    private RawOutput renderedOutput;

    private Throwable failureCause;            // failure info if execution failed

    /**
     * Creates a new HorizonContext with the given raw input.
     *
     * @param rawInput the raw input for this context
     * @throws NullPointerException if rawInput is null
     */
    public HorizonContext(RawInput rawInput) {
        this.rawInput = Objects.requireNonNull(rawInput, "rawInput must not be null");
        this.traceId = UUID.randomUUID().toString();
        this.scheme = rawInput.getScheme();
        this.source = rawInput.getSource();
    }

    /**
     * Private constructor used by the builder and copy method.
     */
    private HorizonContext(Builder builder) {
        this.traceId = builder.traceId;
        this.scheme = builder.scheme;
        this.source = builder.source;
        this.rawInput = builder.rawInput;
        this.parsedIntent = builder.parsedIntent;
        this.intentPayload = builder.intentPayload;
        this.executionResult = builder.executionResult;
        this.renderedOutput = builder.renderedOutput;
        this.failureCause = builder.failureCause;
    }

    /**
     * Returns the trace ID of this context.
     * The trace ID uniquely identifies this flow and can be used for tracking and debugging.
     *
     * @return the trace ID
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * Returns the scheme of this context.
     * The scheme represents the protocol or format of the input (e.g., http, cli, ws).
     *
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the source of this context.
     * The source typically represents the origin or sender of the input.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the raw input of this context.
     *
     * @return the raw input
     */
    public RawInput getRawInput() {
        return rawInput;
    }

    /**
     * Returns the parsed intent of this context.
     * The parsed intent represents the interpreted meaning of the input.
     *
     * @return the parsed intent, or null if no intent has been parsed
     */
    public String getParsedIntent() {
        return parsedIntent;
    }

    /**
     * Sets the parsed intent of this context.
     *
     * @param parsedIntent the parsed intent to set
     */
    public void setParsedIntent(String parsedIntent) {
        this.parsedIntent = parsedIntent;
    }

    /**
     * Returns the intent payload of this context.
     * The intent payload contains structured data for downstream processing.
     *
     * @return the intent payload, or null if no payload has been set
     */
    public Object getIntentPayload() {
        return intentPayload;
    }

    /**
     * Sets the intent payload of this context.
     *
     * @param intentPayload the intent payload to set
     */
    public void setIntentPayload(Object intentPayload) {
        this.intentPayload = intentPayload;
    }

    /**
     * Returns the execution result of this context.
     * The execution result represents the outcome of intent realization within the system.
     *
     * @return the execution result, or null if no result has been set
     */
    public Object getExecutionResult() {
        return executionResult;
    }

    /**
     * Sets the execution result of this context.
     *
     * @param executionResult the execution result to set
     */
    public void setExecutionResult(Object executionResult) {
        this.executionResult = executionResult;
    }

    /**
     * Returns the rendered output of this context.
     * The rendered output contains the finalized result for external output.
     *
     * @return the rendered output, or null if no output has been rendered
     */
    public RawOutput getRenderedOutput() {
        return renderedOutput;
    }

    /**
     * Sets the rendered output of this context.
     *
     * @param renderedOutput the rendered output to set
     */
    public void setRenderedOutput(RawOutput renderedOutput) {
        this.renderedOutput = renderedOutput;
    }

    /**
     * Returns the failure cause of this context.
     * The failure cause contains information about any errors that occurred during processing.
     *
     * @return the failure cause, or null if no failure has occurred
     */
    public Throwable getFailureCause() {
        return failureCause;
    }

    /**
     * Sets the failure cause of this context.
     *
     * @param failureCause the failure cause to set
     */
    public void setFailureCause(Throwable failureCause) {
        this.failureCause = failureCause;
    }

    /**
     * Returns whether this context has failed.
     * A context is considered failed if it has a non-null failure cause.
     *
     * @return true if this context has failed, false otherwise
     */
    public boolean hasFailed() {
        return failureCause != null;
    }

    /**
     * Creates a copy of this context.
     * The copy will have the same values for all fields as this context.
     *
     * @return a copy of this context
     */
    public HorizonContext copy() {
        return new Builder()
                .traceId(this.traceId)
                .scheme(this.scheme)
                .source(this.source)
                .rawInput(this.rawInput)
                .parsedIntent(this.parsedIntent)
                .intentPayload(this.intentPayload)
                .executionResult(this.executionResult)
                .renderedOutput(this.renderedOutput)
                .failureCause(this.failureCause)
                .build();
    }

    /**
     * Returns a string representation of this context.
     * The string includes the trace ID, scheme, source, and whether the context has failed.
     *
     * @return a string representation of this context
     */
    @Override
    public String toString() {
        return "HorizonContext{" +
                "traceId='" + traceId + '\'' +
                ", scheme='" + scheme + '\'' +
                ", source='" + source + '\'' +
                ", parsedIntent='" + parsedIntent + '\'' +
                ", hasFailed=" + hasFailed() +
                '}';
    }

    /**
     * Returns a new builder for creating HorizonContext instances.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for creating HorizonContext instances.
     */
    public static class Builder {
        private String traceId;
        private String scheme;
        private String source;
        private RawInput rawInput;
        private String parsedIntent;
        private Object intentPayload;
        private Object executionResult;
        private RawOutput renderedOutput;
        private Throwable failureCause;

        /**
         * Sets the trace ID for the context being built.
         *
         * @param traceId the trace ID to set
         * @return this builder
         */
        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * Sets the scheme for the context being built.
         *
         * @param scheme the scheme to set
         * @return this builder
         */
        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Sets the source for the context being built.
         *
         * @param source the source to set
         * @return this builder
         */
        public Builder source(String source) {
            this.source = source;
            return this;
        }

        /**
         * Sets the raw input for the context being built.
         *
         * @param rawInput the raw input to set
         * @return this builder
         */
        public Builder rawInput(RawInput rawInput) {
            this.rawInput = rawInput;
            return this;
        }

        /**
         * Sets the parsed intent for the context being built.
         *
         * @param parsedIntent the parsed intent to set
         * @return this builder
         */
        public Builder parsedIntent(String parsedIntent) {
            this.parsedIntent = parsedIntent;
            return this;
        }

        /**
         * Sets the intent payload for the context being built.
         *
         * @param intentPayload the intent payload to set
         * @return this builder
         */
        public Builder intentPayload(Object intentPayload) {
            this.intentPayload = intentPayload;
            return this;
        }

        /**
         * Sets the execution result for the context being built.
         *
         * @param executionResult the execution result to set
         * @return this builder
         */
        public Builder executionResult(Object executionResult) {
            this.executionResult = executionResult;
            return this;
        }

        /**
         * Sets the rendered output for the context being built.
         *
         * @param renderedOutput the rendered output to set
         * @return this builder
         */
        public Builder renderedOutput(RawOutput renderedOutput) {
            this.renderedOutput = renderedOutput;
            return this;
        }

        /**
         * Sets the failure cause for the context being built.
         *
         * @param failureCause the failure cause to set
         * @return this builder
         */
        public Builder failureCause(Throwable failureCause) {
            this.failureCause = failureCause;
            return this;
        }

        /**
         * Builds a new HorizonContext with the values set in this builder.
         *
         * @return a new HorizonContext
         * @throws NullPointerException if rawInput is null
         */
        public HorizonContext build() {
            Objects.requireNonNull(rawInput, "rawInput must not be null");
            if (traceId == null) {
                traceId = UUID.randomUUID().toString();
            }
            if (scheme == null) {
                scheme = rawInput.getScheme();
            }
            if (source == null) {
                source = rawInput.getSource();
            }
            return new HorizonContext(this);
        }
    }
}
