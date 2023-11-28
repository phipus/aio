package com.github.phipus.aio.iter;

public class IteratorItem<T> {

    private final T value;
    private final boolean valid;

    public IteratorItem(T value, boolean valid) {
        this.value = value;
        this.valid = valid;
    }

    public T getValue() {
        return value;
    }

    public boolean isNotValid() {
        return !valid;
    }

    public boolean isValid() {
        return valid;
    }
}
