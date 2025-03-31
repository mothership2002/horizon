package horizon.engine.servlet;

import horizon.core.context.HorizonContext;
import horizon.core.model.input.http.HttpRawInput;
import horizon.core.model.output.http.HttpRawOutput;
import horizon.engine.ServerEngine;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    @Override
    protected void doStart(HorizonContext<HttpRawInput, HttpRawOutput> context, int port) throws Exception {

    }
}
