package com.github.anrimian.musicplayer.domain.utils.functions;

public interface BiFunction<T, P, L> {

    L call(T obj1, P obj2);
}
