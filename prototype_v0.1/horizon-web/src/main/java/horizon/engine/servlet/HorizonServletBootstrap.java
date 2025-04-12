package horizon.engine.servlet;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    /**
     * Constructs a new HorizonServletBootstrap using the specified HTTP context.
     *
     * @param context the HTTP context containing configurations and I/O details for raw HTTP processing
     */
    protected HorizonServletBootstrap(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) {
        super(context);
    }

    /**
     * Starts the servlet bootstrap. This implementation does not perform any startup actions.
     *
     * @param context the context providing HTTP raw input and output configurations
     * @throws Exception if an error occurs during startup
     */
    @Override
    protected void doStart(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) throws Exception {

    }
}
