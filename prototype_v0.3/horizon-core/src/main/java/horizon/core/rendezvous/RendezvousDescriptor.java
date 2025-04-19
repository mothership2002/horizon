package horizon.core.rendezvous;

import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;

public class RendezvousDescriptor<I extends RawInput, O extends RawOutput> {

    private final String scheme;
    private final AbstractRendezvous<I, O> rendezvous;
    private final Class<I> inputType;
    private final Class<O> outputType;

    public RendezvousDescriptor(String scheme, AbstractRendezvous<I, O> rendezvous, Class<I> inputType, Class<O> outputType) {
        this.scheme = scheme;
        this.rendezvous = rendezvous;
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public String getScheme() {
        return scheme;
    }

    public AbstractRendezvous<I, O> getRendezvous() {
        return rendezvous;
    }

    public Class<I> getInputType() {
        return inputType;
    }

    public Class<O> getOutputType() {
        return outputType;
    }
}
