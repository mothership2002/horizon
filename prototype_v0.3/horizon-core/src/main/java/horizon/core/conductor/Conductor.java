package horizon.core.conductor;

import horizon.core.command.Command;

public interface Conductor<P> {
    Command resolve(P payload);
}
