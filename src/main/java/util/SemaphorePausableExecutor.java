package util;

import java.util.concurrent.*;

import java.util.concurrent.*;
import java.util.logging.Level; // Import Level
import java.util.logging.Logger; // Import Logger

public class SemaphorePausableExecutor extends ThreadPoolExecutor {
    // 1. Add a Logger instance
    private static final Logger logger = Logger.getLogger(SemaphorePausableExecutor.class.getName());

    private final Semaphore executionGate;
    private final int configuredPoolSize;

    public SemaphorePausableExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                     TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                     ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.configuredPoolSize = corePoolSize;
        this.executionGate = new Semaphore(this.configuredPoolSize, true);

        // Log creation details
        logger.log(Level.INFO, "SemaphorePausableExecutor created. Pool Size: {0}, Initial Permits: {1}, Fairness: {2}",
                new Object[]{this.configuredPoolSize, this.executionGate.availablePermits(), true});
    }

    public SemaphorePausableExecutor(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    @Override
    protected void beforeExecute(Thread workerThread, Runnable task) {
        super.beforeExecute(workerThread, task);
        boolean permitAcquired = false;
        while (!permitAcquired && !isShutdown()) {
            try {
                logger.log(Level.FINER, "{0} attempting to acquire execution permit. Available: {1}",
                        new Object[]{workerThread.getName(), executionGate.availablePermits()});
                executionGate.acquire();
                permitAcquired = true;
                logger.log(Level.FINEST, "{0} execution permit acquired.", workerThread.getName());
            } catch (InterruptedException e) {
                workerThread.interrupt();
                logger.log(Level.WARNING, "{0} interrupted while acquiring execution permit.", workerThread.getName());
                return;
            }
        }
        if (!permitAcquired) {
            logger.log(Level.WARNING, "{0} did not acquire permit (pool likely shutting down), task will not run.", workerThread.getName());
        }
    }

    @Override
    protected void afterExecute(Runnable task, Throwable thrown) {
        super.afterExecute(task, thrown);
        executionGate.release();
        logger.log(Level.FINEST, "{0} execution permit released. Available: {1}",
                new Object[]{Thread.currentThread().getName(), executionGate.availablePermits()});

        if (thrown != null) {
            logger.log(Level.SEVERE, "Task executed by " + Thread.currentThread().getName() + " threw an exception.", thrown);
        }
    }

    public void pauseExecutorForBattle() {
        logger.log(Level.INFO, "Attempting to pause executor by draining permits...");
        executionGate.drainPermits();
        logger.log(Level.INFO, "Executor paused. Available permits now: {0}", executionGate.availablePermits());
    }

    public void resumeExecutorAfterBattle() {
        logger.log(Level.INFO, "Attempting to resume executor by releasing permits...");
        int permitsToRelease = this.configuredPoolSize - executionGate.availablePermits();
        if (permitsToRelease > 0) {
            executionGate.release(permitsToRelease);
        }
        logger.log(Level.INFO, "Executor resumed. Available permits now: {0}", executionGate.availablePermits());
    }
}