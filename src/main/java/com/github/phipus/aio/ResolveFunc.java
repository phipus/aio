package com.github.phipus.aio;

public interface ResolveFunc<T, R> {
    Promise<R> invoke(T value);
}
