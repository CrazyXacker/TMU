package org.telegram.telegraph;

import org.telegram.telegraph.executors.DefaultTelegraphExecutorFactory;

/**
 * Created by rubenlagus on 15/12/2016.
 */
public class TelegraphContext {
    private static final Object lock = new Object();
    private static DefaultTelegraphExecutorFactory INSTANCE;

    public static DefaultTelegraphExecutorFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    INSTANCE = new DefaultTelegraphExecutorFactory();
                }
            }
        }
        return INSTANCE;
    }
}
