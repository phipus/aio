package com.github.phipus.aio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Promise<T> {
    public static final int PENDING = 0;
    public static final int RESOLVED = 1;
    public static final int REJECTED = 2;

    private int state = PENDING;
    private T value;
    private Throwable exception;

    private final ArrayList<CompletionCallback<T>> callbacks = new ArrayList<>();

    public static <R> Promise<R> resolve(R value) {
        Promise<R> p = new Promise<>();
        p.value = value;
        p.state = RESOLVED;
        return p;
    }

    public static <R> Promise<R> reject(Throwable exception) {
        Promise<R> p = new Promise<>();
        p.exception = exception;
        p.state = REJECTED;
        return p;
    }

    public static <R> Promise<R> race(List<Promise<R>> promises) {
        Promise<R> result = new Promise<>();

        for (Promise<R> p : promises) {
            p.applyCallback((exc, value) -> {
                if (exc == null) {
                    result.setResolved(value);
                } else {
                    result.setRejected(exc);
                }
            });
        }

        return result;
    }

    public static <R> PromiseBuilder<R, R> race() {
        ArrayList<Promise<R>> promises = new ArrayList<>();
        return new PromiseBuilder<>() {
            @Override
            public PromiseBuilder<R, R> add(Promise<R> p) {
                promises.add(p);
                return this;
            }

            @Override
            public Promise<R> build() {
                return Promise.race(promises);
            }
        };
    }

    public static <R> Promise<List<R>> all(List<Promise<R>> promises) {
        final ArrayList<R> resultList = new ArrayList<>(promises.size());
        final Promise<List<R>> result = new Promise<>();

        for (int i = 0; i < promises.size(); i++) {
            resultList.add(null);
        }

        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < promises.size(); i++) {
            final int j = i;
            promises.get(i).applyCallback(((exc, value) -> {
                if (exc != null) {
                    result.setRejected(exc);
                } else {
                    resultList.set(j, value);
                    int newCompleted = completed.addAndGet(1);
                    if (newCompleted >= resultList.size()) {
                        result.setResolved(resultList);
                    }
                }
            }));
        }

        return result;
    }

    public static <T> PromiseBuilder<T, List<T>> all() {
        ArrayList<Promise<T>> promises = new ArrayList<>();
        return new PromiseBuilder<>() {
            @Override
            public PromiseBuilder<T, List<T>> add(Promise<T> p) {
                promises.add(p);
                return this;
            }

            @Override
            public Promise<List<T>> build() {
                return Promise.all(promises);
            }
        };
    }

    public static <R> Promise<List<Promise<R>>> allSettled(List<Promise<R>> promises) {
        Promise<List<Promise<R>>> result = new Promise<>();

        AtomicInteger completed = new AtomicInteger(0);

        for (int i = 0; i < promises.size(); i++) {
            promises.get(i).applyCallback((exc, value) -> {
                int newCompleted = completed.addAndGet(1);
                if (newCompleted >= promises.size()) {
                    result.setResolved(promises);
                }
            });
        }

        return result;
    }

    public static <R> PromiseBuilder<R, List<Promise<R>>> allSettled() {
        ArrayList<Promise<R>> promises = new ArrayList<>();
        return new PromiseBuilder<>() {
            @Override
            public PromiseBuilder<R, List<Promise<R>>> add(Promise<R> p) {
                promises.add(p);
                return this;
            }

            @Override
            public Promise<List<Promise<R>>> build() {
                return Promise.allSettled(promises);
            }
        };
    }

    public void applyCallback(CompletionCallback<T> cb) {
        synchronized (this) {
            if (state == PENDING) {
                callbacks.add(cb);
                return;
            }
        }

        // state is not changed if it is not pending
        switch (state) {
            case RESOLVED:
                cb.invoke(null, value);
                break;

            case REJECTED:
                cb.invoke(exception, null);
                break;
        }
    }

    public <R> Promise<R> chain(CompletionFunc<T, R> onComplete) {
        synchronized (this) {
            if (state == PENDING) {
                Promise<R> p = new Promise<>();
                callbacks.add((exc, value) -> {
                    try {
                        Promise<R> next = onComplete.invoke(exc, value);

                        if (next == null)
                            next = Promise.resolve(null);

                        next.applyCallback((nextExc, nextValue) -> {
                            if (nextExc != null) {
                                p.setRejected(nextExc);
                                return;
                            }
                            p.setResolved(nextValue);
                        });
                    } catch (Throwable ex) {
                        p.setRejected(ex);
                    }
                });
                return p;
            }
        }

        // if state is not pending, it can not change thus no locking needed
        Promise<R> next;
        switch (state) {
            case RESOLVED:
                next = onComplete.invoke(null, value);
                break;
            case REJECTED:
                next = onComplete.invoke(exception, null);
                break;
            default:
                throw new RuntimeException("aio.com.github.phipus.aio.Promise has invalid state");
        }

        return next == null ? Promise.resolve(null) : next;
    }

    public <R> Promise<R> then(ResolveFunc<T, R> onComplete) {
        return chain((exc, value) -> exc != null ? Promise.reject(exc) : onComplete.invoke(value));
    }

    public void thenCallback(ResolveCallback<T> onComplete) {
        applyCallback((exc, value) -> {
            if (exc == null)
                onComplete.invoke(value);
        });
    }

    public Promise<T> except(RejectFunc<T> onComplete) {
        return chain((exc, value) -> exc == null ? Promise.resolve(value) : onComplete.invoke(exc));
    }

    public void exceptCallback(RejectCallback onComplete) {
        applyCallback((exc, value) -> {
            if (exc != null)
                onComplete.invoke(exc);
        });
    }

    private void setResolved(T value) {
        synchronized (this) {
            if (state != PENDING)
                return;

            state = RESOLVED;
            this.value = value;
        }
        // callbacks are not changed after state has been set
        for (CompletionCallback<T> cb : callbacks) {
            Loop.schedule(() -> cb.invoke(null, value));
        }
    }

    private void setRejected(Throwable exc) {
        synchronized (this) {
            if (state != PENDING)
                return;

            state = REJECTED;
            this.exception = exc;
        }

        // callbacks are not changed after state has been set
        for (CompletionCallback<T> cb : callbacks) {
            Loop.schedule(() -> cb.invoke(exc, null));
        }
    }

    private Promise() {

    }


    public Promise(ExecutorCallback<T> executor) {
        Loop.schedule(() -> {
            try {
                executor.invoke(this::setResolved, this::setRejected);
            } catch (Throwable exc) {
                this.setRejected(exc);
            }
        });
    }
}
