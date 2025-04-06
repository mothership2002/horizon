package horizon.engine.servlet;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    /**
     * Constructs a new HorizonServletBootstrap with the specified context.
     *
     * <p>This constructor initializes the servlet bootstrap by passing the given abstract context,
     * which encapsulates HTTP raw input and output configurations, to its superclass.</p>
     *
     * @param context the abstract context providing HTTP raw input and output configuration
     */
    protected HorizonServletBootstrap(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) {
        super(context);
    }

    /**
     * Initiates the servlet's start-up process.
     *
     * <p>This method is called during server start-up and is intended for any servlet-specific
     * initialization. In the current implementation, no start-up actions are performed.</p>
     *
     * @param context the horizon context providing HTTP raw input and output handlers
     * @throws Exception if an error occurs during the start-up sequence
     */
    @Override
    protected void doStart(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) throws Exception {

    }
}
