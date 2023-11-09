package com.github.phipus.aio;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Loop {
    public static void schedule(Runnable cb) {
        pool.execute(cb);
    }

    public static void quit() {
        Delay.quit();
        pool.shutdown();
    }

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 16, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
}
