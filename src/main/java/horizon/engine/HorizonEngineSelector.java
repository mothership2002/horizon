package horizon.engine;

public class HorizonEngineSelector {

    public static ServerEngine select() {
        if (isPresent("io.netty.bootstrap.ServerBootstrap")) {
            return new horizon.engine.netty.HorizonNettyBootstrap();
        }

        if (isPresent("jakarta.servlet.Servlet")
                || isPresent("javax.servlet.Servlet")) {
            return new horizon.engine.servlet.HorizonServletBootstrap();
        }

        throw new IllegalStateException("No supported Horizon engine found in classpath.");
    }

    private static boolean isPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
