package com.github.phipus.aio;

import java.util.HashSet;

public class Event<T> {
    private final HashSet<EventCallback<T>> callbacks = new HashSet<>();


    public boolean addListener(EventCallback<T> cb) {
        synchronized (this) {
            return callbacks.add(cb);
        }
    }

    public boolean removeListener(EventCallback<T> cb) {
        synchronized (this) {
            return callbacks.remove(cb);
        }
    }

    public void fire(T value) {
        synchronized (this) {
            callbacks.forEach(cb -> {
                Loop.schedule(() -> {
                    cb.invoke(value);
                });
            });
        }
    }
}
