package horizon;

import horizon.engine.netty.HorizonNettyBootstrap;

public class HorizonApplication {

    public static void main(String[] args) throws InterruptedException {
        new HorizonNettyBootstrap().start(8080);
    }
}
