package com.github.anrimian.musicplayer.domain.utils.functions;

public class Optional<T> {

    private final T value;

    public Optional(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optional<?> optional = (Optional<?>) o;

        return value != null ? value.equals(optional.value) : optional.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Optional{" +
                "value=" + value +
                '}';
    }
}
