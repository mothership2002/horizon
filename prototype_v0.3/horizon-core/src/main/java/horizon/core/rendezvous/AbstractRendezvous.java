package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;

import java.util.List;

public abstract class AbstractRendezvous<I extends RawInput, N, K, P > implements Rendezvous<I, K, P> {

    protected Foyer<I> foyer;
    protected List<Sentinel<I>> sentinels;
    protected Normalizer<I, N> normalizer;
    protected Interpreter<N, K, P> interpreter;

    public AbstractRendezvous(Foyer<I> foyer, List<Sentinel<I>> sentinels, Normalizer<I, N> normalizer, Interpreter<N, K, P> interpreter) {
        this.foyer = foyer;
        this.sentinels = sentinels;
        this.normalizer = normalizer;
        this.interpreter = interpreter;
    }

    public void receive(I input, HorizonContext context) {
        if (!foyer.allow(input)) {
            throw new SecurityException("Input not allowed");
        }
        for (Sentinel<I> sentinel : sentinels) {
            sentinel.inspect(input);
        }

        N normalized = normalizer.normalize(input);
        K key = interpreter.extractIntentKey(normalized);
        P payload = interpreter.extractIntentPayload(normalized);

        context.setParsedIntent(key.toString());
        context.setIntentPayload(payload);
    }
}