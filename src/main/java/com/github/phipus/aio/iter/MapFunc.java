package com.github.phipus.aio.iter;

public interface MapFunc<T, E> {
    E invoke(T value);
}
