package com.github.phipus.aio;

public interface PromiseBuilder<T, R> {
    PromiseBuilder<T, R> add(Promise<T> p);
    Promise<R> build();
}
