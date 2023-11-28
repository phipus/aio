package com.github.phipus.aio;

public interface EventCallback<T> {
    void invoke(T v);
}
