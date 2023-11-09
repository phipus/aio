package com.github.phipus.aio;

import java.util.Timer;
import java.util.TimerTask;

public class Delay {
    public static Promise<Object> milliseconds(long ms) {
        return new Promise<>(((resolve, reject) -> timer.schedule(new TimerTask() {
            @Override
            public void run() {
                resolve.invoke(null);
            }
        }, ms)));
    }

    public static void quit() {
        timer.cancel();
    }

    private static final Timer timer = new Timer();
}
