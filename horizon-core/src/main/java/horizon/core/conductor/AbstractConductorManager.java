package horizon.core.conductor;

import horizon.core.conductor.gardian.Guardian;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractConductorManager implements ConductorManager {

    protected final List<Guardian> guardians = new LinkedList<>();

}
