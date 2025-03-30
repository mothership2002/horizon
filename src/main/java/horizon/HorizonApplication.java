package horizon;

import horizon.core.context.HorizonContextCoordinator;
import horizon.core.context.NettyEngineContext;

public class HorizonApplication {

    public static void main(String[] args) throws Exception {
        HorizonContextCoordinator coordinator = new HorizonContextCoordinator();
        coordinator.register(new NettyEngineContext());
        // TODO multi protocol server
        coordinator.runAll(8080);
    }
}
