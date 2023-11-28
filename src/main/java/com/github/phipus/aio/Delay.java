package com.github.phipus.aio;

import java.util.Timer;
import java.util.TimerTask;

public class Delay {
    public static Promise<Object> milliseconds(long milliseconds) {
        return new Promise<>(((resolve, reject) ->
                Loop.scheduleLater(milliseconds, () -> resolve.invoke(null))
        ));
    }
}
