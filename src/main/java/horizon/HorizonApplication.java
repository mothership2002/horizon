package horizon;

import horizon.engine.HorizonEngineSelector;

public class HorizonApplication {

    public static void main(String[] args) throws Exception {
        HorizonEngineSelector.select().start(8080);
    }
}
