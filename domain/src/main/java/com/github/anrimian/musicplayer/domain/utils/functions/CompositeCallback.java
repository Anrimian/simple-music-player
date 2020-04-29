package com.github.anrimian.musicplayer.domain.utils.functions;

import java.util.LinkedList;

public class CompositeCallback<T> implements Callback<T> {

    private final LinkedList<Callback<T>> callbacks = new LinkedList<>();

    public void add(Callback<T> callback) {
        callbacks.add(callback);
    }

    public void call(T value) {
        for (Callback<T> callback: callbacks) {
            callback.call(value);
        }
    }
}
