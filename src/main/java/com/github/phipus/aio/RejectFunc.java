package com.github.phipus.aio;

public interface RejectFunc<R> {
    Promise<R> invoke(Throwable exc);
}
