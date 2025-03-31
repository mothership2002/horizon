package horizon.engine.servlet;

import horizon.core.context.HorizonContext;
import horizon.core.input.http.HttpRawInput;
import horizon.engine.ServerEngine;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput> {

    @Override
    protected void doStart(HorizonContext<HttpRawInput> context, int port) throws Exception {

    }
}
