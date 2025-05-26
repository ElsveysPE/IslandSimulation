package util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class NamedThreadFactory implements ThreadFactory {
    private static final Logger logger = Logger.getLogger(NamedThreadFactory.class.getName());
    private final String namePrefix;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix + "-thread-";
        this.uncaughtExceptionHandler = (thread, throwable) -> {
            logger.severe("Uncaught exception in thread '" + thread.getName() + "': " + throwable.getMessage());
        };
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        Thread t = new Thread(r, threadName);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        t.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        logger.fine("Created new thread: " + threadName);
        return t;
    }
}