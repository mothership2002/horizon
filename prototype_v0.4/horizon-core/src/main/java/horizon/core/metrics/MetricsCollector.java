package horizon.core.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Simple metrics collector for monitoring framework performance.
 */
public class MetricsCollector {
    private static final MetricsCollector INSTANCE = new MetricsCollector();
    
    private final Map<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> gauges = new ConcurrentHashMap<>();
    private final Map<String, TimingStats> timings = new ConcurrentHashMap<>();
    
    private MetricsCollector() {}
    
    public static MetricsCollector getInstance() {
        return INSTANCE;
    }
    
    /**
     * Increment a counter metric.
     */
    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new LongAdder()).increment();
    }
    
    /**
     * Record a timing in milliseconds.
     */
    public void recordTiming(String name, long milliseconds) {
        timings.computeIfAbsent(name, k -> new TimingStats()).record(milliseconds);
    }
    
    /**
     * Set a gauge value.
     */
    public void setGauge(String name, long value) {
        gauges.computeIfAbsent(name, k -> new AtomicLong()).set(value);
    }
    
    /**
     * Get all metrics as a map.
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Counters
        counters.forEach((name, counter) -> 
            metrics.put("counter." + name, counter.sum()));
        
        // Gauges
        gauges.forEach((name, gauge) -> 
            metrics.put("gauge." + name, gauge.get()));
        
        // Timings
        timings.forEach((name, stats) -> {
            metrics.put("timing." + name + ".count", stats.getCount());
            metrics.put("timing." + name + ".mean", stats.getMean());
            metrics.put("timing." + name + ".max", stats.getMax());
            metrics.put("timing." + name + ".min", stats.getMin());
        });
        
        return metrics;
    }
    
    /**
     * Reset all metrics.
     */
    public void reset() {
        counters.clear();
        gauges.clear();
        timings.clear();
    }
    
    /**
     * Simple timing statistics.
     */
    private static class TimingStats {
        private final LongAdder count = new LongAdder();
        private final LongAdder sum = new LongAdder();
        private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);
        private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        
        void record(long value) {
            count.increment();
            sum.add(value);
            
            // Update max
            long currentMax;
            do {
                currentMax = max.get();
            } while (value > currentMax && !max.compareAndSet(currentMax, value));
            
            // Update min
            long currentMin;
            do {
                currentMin = min.get();
            } while (value < currentMin && !min.compareAndSet(currentMin, value));
        }
        
        long getCount() { return count.sum(); }
        double getMean() { 
            long c = count.sum();
            return c > 0 ? (double) sum.sum() / c : 0;
        }
        long getMax() { return max.get() == Long.MIN_VALUE ? 0 : max.get(); }
        long getMin() { return min.get() == Long.MAX_VALUE ? 0 : min.get(); }
    }
}
