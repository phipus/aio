package com.github.phipus.aio;

public interface CompletionCallback<T> {
    void invoke(Throwable exc, T value);
}
