package com.github.phipus.aio;

public interface ExecutorCallback<T> {
    void invoke(ResolveCallback<T> resolve, RejectCallback reject);
}
