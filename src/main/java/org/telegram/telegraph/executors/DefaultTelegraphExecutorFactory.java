package org.telegram.telegraph.executors;

import org.telegram.telegraph.ExecutorOptions;

/**
 * @author Ruben Bermudez
 * @version 1.0
 */
public class DefaultTelegraphExecutorFactory implements TelegraphExecutorFactory {
    private TelegraphExecutor executor;
    private static final Object LOCK = new Object();

    public DefaultTelegraphExecutorFactory() {
        if (executor == null) {
            synchronized (LOCK) {
                if (executor == null) {
                    this.executor = new DefaultTelegraphExecutor(new ExecutorOptions());
                }
            }
        }
    }

    @Override
    public TelegraphExecutor getExecutor() {
        return executor;
    }
}
