package horizon;

import horizon.core.annotation.HorizonApplication;
import horizon.core.context.HorizonContextCoordinator;
import horizon.context.NettyEngineContext;

@HorizonApplication
public class HorizonApplicationDemo {

    public static void main(String[] args) throws Exception {
        HorizonContextCoordinator coordinator = new HorizonContextCoordinator();
        coordinator.register(new NettyEngineContext(10, 8080));
        // TODO multi protocol server
        coordinator.runAll();
    }
}
