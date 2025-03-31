package horizon;

import horizon.core.annotation.HorizonApplication;
import horizon.core.context.HorizonContextCoordinator;
import horizon.core.context.NettyEngineContext;

@HorizonApplication
public class HorizonApplicationDemo {

    public static void main(String[] args) throws Exception {
        HorizonContextCoordinator coordinator = new HorizonContextCoordinator();
        coordinator.register(new NettyEngineContext());
        // TODO multi protocol server
        coordinator.runAll(8080);
    }
}
