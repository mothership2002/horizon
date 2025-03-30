package horizon.engine.servlet;

import horizon.core.context.HorizonContext;
import horizon.core.input.http.HttpRawInput;
import horizon.engine.ServerEngineTemplate;

public class HorizonServletBootstrap extends ServerEngineTemplate<HttpRawInput> {

    @Override
    protected void doStart(HorizonContext<HttpRawInput> context, int port) throws Exception {

    }
}
