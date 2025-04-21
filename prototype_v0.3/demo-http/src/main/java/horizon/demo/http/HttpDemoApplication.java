package horizon.demo.http;

import horizon.core.constant.Scheme;
import horizon.core.context.HorizonRuntimeUnit;
import horizon.core.context.HorizonSystemContext;
import horizon.core.engine.HorizonFlowEngine;
import horizon.core.model.HorizonContext;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.RendezvousDescriptor;
import horizon.core.stage.StageHandler;
import horizon.http.netty.NettyHttpAdapter;
import horizon.http.netty.NettyHttpFoyer;
import horizon.http.simple.SimpleHttpInput;
import horizon.http.simple.SimpleHttpInputConverter;
import horizon.http.simple.SimpleHttpOutput;
import horizon.http.simple.SimpleHttpRendezvous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * A simple demo application that sets up an HTTP server using the Horizon Framework.
 * This version integrates the Flow Engine to process requests.
 */
public class HttpDemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDemoApplication.class.getName());
    private static final int HTTP_PORT = 8080;

    public static void main(String[] args) {
        LOGGER.info("Starting HTTP Demo Application with Flow Engine");

        try {
            // Create the system context
            HorizonSystemContext systemContext = new HorizonSystemContext();
            systemContext.initialize();

            // Create the rendezvous
            SimpleHttpRendezvous rendezvous = new SimpleHttpRendezvous();

            // Create the rendezvous descriptor
            RendezvousDescriptor<SimpleHttpInput, SimpleHttpInput, String, Map<String, Object>, SimpleHttpOutput> descriptor = 
                new RendezvousDescriptor<>(
                    Scheme.HTTP.name(),
                    rendezvous,
                    SimpleHttpInput.class,
                    SimpleHttpOutput.class
                );

            // Create the runtime unit
            HorizonRuntimeUnit<SimpleHttpInput, SimpleHttpInput, String, Map<String, Object>, SimpleHttpOutput> runtimeUnit = 
                new HorizonRuntimeUnit<>(descriptor);

            // Create a simple stage handler that returns the rendered output from the context
            StageHandler defaultStageHandler = new StageHandler() {
                @Override
                public RawOutput handle(HorizonContext context) {
                    // If the context has a rendered output, return it
                    if (context.getRenderedOutput() != null) {
                        return context.getRenderedOutput();
                    }

                    // If the context has an execution result, create a SimpleHttpOutput from it
                    Object result = context.getExecutionResult();
                    if (result != null) {
                        return new SimpleHttpOutput(result.toString(), "text/plain");
                    }

                    // If the context has a failure cause, create an error response
                    if (context.getFailureCause() != null) {
                        return new SimpleHttpOutput("Error: " + context.getFailureCause().getMessage(), 500, "text/plain");
                    }

                    // Default response
                    return new SimpleHttpOutput("No content", 204, "text/plain");
                }
            };

            // Register the default stage handler
            runtimeUnit.registerCentralStage("default", defaultStageHandler);

            // Register the UserConductor
            UserConductor userConductor = new UserConductor();
            runtimeUnit.registerConductor("users", userConductor);

            // Register the runtime unit with the system context
            systemContext.registerUnit(Scheme.HTTP, runtimeUnit);

            // Initialize the Flow Engine in the system context
            systemContext.initializeFlowEngine();

            // Create the input converter
            SimpleHttpInputConverter inputConverter = new SimpleHttpInputConverter();

            // Create the adapter
            NettyHttpAdapter<SimpleHttpInput, SimpleHttpOutput> adapter =
                new NettyHttpAdapter<>(inputConverter);

            // Create the foyer with system context
            NettyHttpFoyer<SimpleHttpInput, SimpleHttpOutput> foyer = 
                new NettyHttpFoyer<>(HTTP_PORT, rendezvous, adapter, systemContext);

            // Initialize the foyer (starts the server)
            foyer.initialize();

            LOGGER.info("HTTP server started on port " + HTTP_PORT + " with Flow Engine");
            LOGGER.info("Available endpoints:");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/ - Welcome message");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/echo - Echo back request body");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/json - Return JSON response");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/flow - Example using FlowEngine");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/users - User management API");

            // Add shutdown hook to stop the server when the application is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down HTTP server and Flow Engine");
                foyer.shutdown();
                systemContext.shutdown();
            }));

            // Keep the application running
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("Error starting HTTP server: {}", e.getMessage());
            LOGGER.error("", e);
        }
    }
}
