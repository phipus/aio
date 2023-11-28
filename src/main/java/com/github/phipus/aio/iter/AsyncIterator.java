package com.github.phipus.aio.iter;

import com.github.phipus.aio.CompletionCallback;
import com.github.phipus.aio.Promise;
import com.github.phipus.aio.ResolveCallback;
import com.github.phipus.aio.ResolveFunc;

public interface AsyncIterator<T> {
    Promise<IteratorItem<T>> next();

    default Promise<Void> forEach(ResolveCallback<T> cb) {
        return new Promise<>(((resolve, reject) -> {
            next().applyCallback(new CompletionCallback<IteratorItem<T>>() {
                @Override
                public void invoke(Throwable exc, IteratorItem<T> value) {
                    if (exc != null) {
                        reject.invoke(exc);
                        return;
                    }
                    if (!value.isValid()) {
                        resolve.invoke(null);
                        return;
                    }
                    cb.invoke(value.getValue());
                    next().applyCallback(this);
                }
            });
        }));
    }

    default Promise<Void> forEachAsync(ResolveFunc<T, Void> cb) {
        return new Promise<>((resolve, reject) -> {
            next().applyCallback(new CompletionCallback<IteratorItem<T>>() {
                @Override
                public void invoke(Throwable exc, IteratorItem<T> value) {
                    if (exc != null) {
                        reject.invoke(exc);
                        return;
                    }
                    if (!value.isValid()) {
                        resolve.invoke(null);
                        return;
                    }

                    CompletionCallback<IteratorItem<T>> handleNext = this;

                    cb.invoke(value.getValue()).applyCallback(((exc1, value1) -> {
                        if (exc1 != null) {
                            reject.invoke(exc1);
                            return;
                        }
                        next().applyCallback(handleNext);
                    }));
                }
            });
        });
    }

    default <E> AsyncIterator<E> map(MapFunc<T, E> cb) {
        AsyncIterator<T> source = this;
        return new AsyncIterator<E>() {
            @Override
            public Promise<IteratorItem<E>> next() {
                return new Promise<>(((resolve, reject) -> {
                    source.next().applyCallback(((exc, value) -> {
                        if (exc != null) {
                            reject.invoke(exc);
                            return;
                        }
                        if (!value.isValid()) {
                            resolve.invoke(new IteratorItem<>(null, false));
                            return;
                        }
                        resolve.invoke(new IteratorItem<>(cb.invoke(value.getValue()), true));
                    }));
                }));
            }
        };
    }

    default <E> AsyncIterator<E> mapAsync(ResolveFunc<T, E> cb) {

        AsyncIterator<T> source = this;
        return new AsyncIterator<E>() {
            @Override
            public Promise<IteratorItem<E>> next() {
                return new Promise<>((resolve, reject) -> {
                    source.next().applyCallback(((exc, value) -> {
                        if (exc != null) {
                            reject.invoke(exc);
                            return;
                        }
                        if (!value.isValid()) {
                            resolve.invoke(new IteratorItem<>(null, false));
                            return;
                        }
                        cb.invoke(value.getValue()).applyCallback((exc1, mapped) -> {
                            if (exc1 != null) {
                                reject.invoke(exc1);
                                return;
                            }
                            resolve.invoke(new IteratorItem<>(mapped, true));
                        });
                    }));
                });
            }
        };
    }
}
