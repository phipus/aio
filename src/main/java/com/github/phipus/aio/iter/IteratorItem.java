package com.github.phipus.aio.iter;

public class IteratorItem<T> {

    private T value;
    private boolean valid;

    public IteratorItem(T value, boolean valid) {
        this.value = value;
        this.valid = valid;
    }

    public T getValue() {
        return value;
    }

    public boolean isValid() {
        return valid;
    }
}
