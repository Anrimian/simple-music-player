package com.github.anrimian.musicplayer.domain.utils.functions;

public interface TripleCallback<T, P, M> {

    void call(T obj1, P obj2, M obj3);
}
