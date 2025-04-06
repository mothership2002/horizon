package horizon.engine.servlet;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    /**
     * Initializes a new instance of HorizonServletBootstrap with the specified context.
     *
     * @param context the abstract context encapsulating HTTP raw input and output for servlet operations
     */
    protected HorizonServletBootstrap(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) {
        super(context);
    }

    /**
     * Initiates the servlet engine's startup phase.
     *
     * <p>This overridden method is called during the engine's startup process. In this implementation,
     * no additional startup logic is performed.</p>
     *
     * @param context the context containing HTTP raw input and output handlers
     * @throws Exception if an error occurs during startup initialization
     */
    @Override
    protected void doStart(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) throws Exception {

    }
}
