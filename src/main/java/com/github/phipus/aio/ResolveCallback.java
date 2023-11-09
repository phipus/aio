package com.github.phipus.aio;

public interface ResolveCallback<T> {
    void invoke(T value);
}
