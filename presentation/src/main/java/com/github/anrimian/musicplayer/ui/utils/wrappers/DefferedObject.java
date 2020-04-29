package com.github.anrimian.musicplayer.ui.utils.wrappers;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

import java.util.LinkedList;

import javax.annotation.Nullable;

public class DefferedObject<T> {

    @Nullable
    private T obj;

    private LinkedList<Callback<T>> deferredFunctions = new LinkedList<>();

    public void setObject(@Nullable T obj) {
        this.obj = obj;
        while (!deferredFunctions.isEmpty()) {
            deferredFunctions.pollFirst().call(obj);
        }
    }

    public void call(Callback<T> function) {
        if (obj != null) {
            function.call(obj);
        } else {
            deferredFunctions.add(function);
        }
    }
}
