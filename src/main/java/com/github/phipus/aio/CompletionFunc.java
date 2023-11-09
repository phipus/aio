package com.github.phipus.aio;

public interface CompletionFunc<T, R> {
    Promise<R> invoke(Throwable exc, T value);
}
