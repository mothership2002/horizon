package horizon.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerEngineTemplate implements ServerEngine {

    private static final Logger logger = LoggerFactory.getLogger(ServerEngineTemplate.class);

    protected abstract void doStart(int port) throws Exception;

    public void start(int port) throws Exception {
        long start = System.nanoTime();
        doStart(port);
        logger.info("Horizon Application initialize on port {}", port);
        long end = System.nanoTime();
        logger.info("Horizon Application initialize cost {} ms", (end - start) / 1000000);
    }

}
