package horizon.core.conductor;

import java.lang.annotation.Annotation;
import java.util.Map;

public class ParameterHelper {

    private final Map<Annotation, ParameterAnalyzer> analyzers;

    public ParameterHelper() {
        this.analyzers = analyzers;
    }

    @FunctionalInterface
    public interface ParameterAnalyzer {
        Object analyze(Annotation annotation, Object value);
    }
}
