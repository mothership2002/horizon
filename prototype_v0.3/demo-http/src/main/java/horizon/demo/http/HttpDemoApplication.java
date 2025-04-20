package horizon.demo.http;

import horizon.http.netty.NettyHttpAdapter;
import horizon.http.netty.NettyHttpFoyer;
import horizon.http.simple.SimpleHttpInput;
import horizon.http.simple.SimpleHttpInputConverter;
import horizon.http.simple.SimpleHttpOutput;
import horizon.http.simple.SimpleHttpRendezvous;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple demo application that sets up an HTTP server using the Horizon Framework.
 */
public class HttpDemoApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDemoApplication.class.getName());
    private static final int HTTP_PORT = 8080;

    public static void main(String[] args) {
        LOGGER.info("Starting HTTP Demo Application");

        try {
            // Create the rendezvous
            SimpleHttpRendezvous rendezvous = new SimpleHttpRendezvous();

            // Create the input converter
            SimpleHttpInputConverter inputConverter = new SimpleHttpInputConverter();

            // Create the adapter
            NettyHttpAdapter<SimpleHttpInput, SimpleHttpOutput> adapter =
                new NettyHttpAdapter<>(inputConverter);

            // Create the foyer
            NettyHttpFoyer<SimpleHttpInput, SimpleHttpOutput> foyer = 
                new NettyHttpFoyer<>(HTTP_PORT, rendezvous, adapter);

            // Initialize the foyer (starts the server)
            foyer.initialize();

            LOGGER.info("HTTP server started on port " + HTTP_PORT);
            LOGGER.info("Available endpoints:");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/ - Welcome message");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/echo - Echo back request body");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/json - Return JSON response");
            LOGGER.info("  - http://localhost:" + HTTP_PORT + "/flow - Example using FlowEngine simulation");

            // Add shutdown hook to stop the server when the application is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down HTTP server");
                foyer.shutdown();
            }));

            // Keep the application running
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("Error starting HTTP server: {}", e.getMessage());
            LOGGER.error("", e);
        }
    }
}
