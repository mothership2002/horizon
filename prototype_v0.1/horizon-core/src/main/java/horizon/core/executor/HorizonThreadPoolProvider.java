package horizon.core.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HorizonThreadPoolProvider {

    private final ExecutorService rendezvousExecutor;     // #2 전/후처리
    private final ExecutorService conductorExecutor;      // #3 도메인 로직
    private final ExecutorService stageExecutor;          // #4 중앙 분석/집행

    public HorizonThreadPoolProvider() {
        int core = Runtime.getRuntime().availableProcessors();
        this.rendezvousExecutor = createFlexible("horizon-rendezvous", core * 2, core * 4, 2000);
        this.conductorExecutor = createFlexible("horizon-conductor", core * 2, core * 8, 4000);
        this.stageExecutor = createFlexible("horizon-stage", core * 4, core * 4, 100);
    }

    private ExecutorService createFixed(String name, int poolSize) {
        return Executors.newFixedThreadPool(poolSize, new NamedThreadFactory(name));
    }

    private ExecutorService createFlexible(String name, int coreSize, int maxSize, int queueSize) {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                new NamedThreadFactory(name),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public ExecutorService rendezvous() {
        return rendezvousExecutor;
    }

    public ExecutorService conductor() {
        return conductorExecutor;
    }

    public ExecutorService stage() {
        return stageExecutor;
    }

    public void shutdownAll() {
        rendezvousExecutor.shutdown();
        conductorExecutor.shutdown();
        stageExecutor.shutdown();
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String baseName;
        private final AtomicInteger count = new AtomicInteger(0);

        public NamedThreadFactory(String baseName) {
            this.baseName = baseName;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, baseName + "-" + count.getAndIncrement());
        }
    }
}

