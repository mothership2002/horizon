package horizon.core.rendezvous;

import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

import java.util.List;
import java.util.Objects;

public abstract class AbstractRendezvous<I extends RawInput, N, K, P, O extends RawOutput> implements Rendezvous<I, O> {

    protected final List<Sentinel<I>> sentinels;
    protected final Normalizer<I, N> normalizer;
    protected final Interpreter<N, K, P> interpreter;

    public AbstractRendezvous(List<Sentinel<I>> sentinels, Normalizer<I, N> normalizer, Interpreter<N, K, P> interpreter) {
        this.sentinels = List.copyOf(Objects.requireNonNull(sentinels, "sentinels"));
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer");
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter");
    }

    public HorizonContext encounter(I input) {
        for (Sentinel<I> s : sentinels) s.inspectInbound(input);

        N normalized = normalizer.normalize(input);
        K key = interpreter.extractIntentKey(normalized);
        P payload = interpreter.extractIntentPayload(normalized);

        HorizonContext context = new HorizonContext(input);
        context.setParsedIntent(key != null ? key.toString() : null);
        context.setIntentPayload(payload);
        return context;
    }


    @Override
    public O fallAway(HorizonContext context) {
        for (Sentinel<I> s : sentinels) s.inspectOutbound(context);
        return Objects.requireNonNull(
                (O) context.getRenderedOutput(),
                "No renderedOutput set in HorizonContext"
        );
    }
}