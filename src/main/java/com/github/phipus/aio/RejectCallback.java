package com.github.phipus.aio;

public interface RejectCallback {
    void invoke(Throwable exc);
}
