package com.github.phipus.aio;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Loop {
    public static void schedule(Runnable cb) {
        pool.execute(cb);
    }

    public static void scheduleLater(long milliseconds, Runnable cb) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                schedule(cb);
            }
        }, milliseconds);
    }

    public static void quit() {
        pool.shutdown();
        timer.cancel();
    }

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(8, 16, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private static final Timer timer = new Timer();
}
