package horizon.engine.servlet;

import horizon.core.context.HorizonContext;
import horizon.protocol.http.input.HttpRawInput;
import horizon.protocol.http.output.HttpRawOutput;
import horizon.core.context.ServerEngine;

public class HorizonServletBootstrap extends ServerEngine.ServerEngineTemplate<HttpRawInput, HttpRawOutput> {

    @Override
    protected void doStart(HorizonContext<HttpRawInput, HttpRawOutput> context, int port) throws Exception {

    }
}
