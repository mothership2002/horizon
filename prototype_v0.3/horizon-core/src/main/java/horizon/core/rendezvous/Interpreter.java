package horizon.core.rendezvous;

public interface Interpreter<N, K, P> {

    K extractIntentKey(N normalized);
    P extractIntentPayload(N normalized);
}
