package horizon.engine.servlet;

import horizon.core.context.AbstractHorizonContext;
import horizon.core.context.ServerEngine;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    @Override
    protected void doStart(AbstractHorizonContext<HttpRawInput, HttpRawOutput> context) throws Exception {

    }
}
