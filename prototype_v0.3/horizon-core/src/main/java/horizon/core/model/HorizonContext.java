package horizon.core.model;

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
    private final RawInput originalInput;      // raw input, protocol-agnostic abstraction

    private String parsedIntent;               // interpreted meaning
    private Object intentPayload;              // structured payload for downstream

    // Represents the outcome of intent realization within the system.
    // This is NOT necessarily the final response to the user.
    private Object executionResult;

    // Optional: The system's finalized rendering of the result, for external output.
    private RawOutput renderedOutput;

    private Throwable failureCause;            // failure info if execution failed

    public HorizonContext(String scheme, String source, RawInput originalInput) {
        this.traceId = UUID.randomUUID().toString();
        this.scheme = scheme;
        this.source = source;
        this.originalInput = originalInput;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getScheme() {
        return scheme;
    }

    public String getSource() {
        return source;
    }

    public RawInput getOriginalInput() {
        return originalInput;
    }

    public String getParsedIntent() {
        return parsedIntent;
    }

    public void setParsedIntent(String parsedIntent) {
        this.parsedIntent = parsedIntent;
    }

    public Object getIntentPayload() {
        return intentPayload;
    }

    public void setIntentPayload(Object intentPayload) {
        this.intentPayload = intentPayload;
    }

    public Object getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(Object executionResult) {
        this.executionResult = executionResult;
    }

    public RawOutput getRenderedOutput() {
        return renderedOutput;
    }

    public void setRenderedOutput(RawOutput renderedOutput) {
        this.renderedOutput = renderedOutput;
    }

    public Throwable getFailureCause() {
        return failureCause;
    }

    public void setFailureCause(Throwable failureCause) {
        this.failureCause = failureCause;
    }

    public boolean hasFailed() {
        return failureCause != null;
    }
}
