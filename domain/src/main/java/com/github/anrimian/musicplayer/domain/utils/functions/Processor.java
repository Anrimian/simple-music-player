package com.github.anrimian.musicplayer.domain.utils.functions;

public interface Processor<T> {
    T call(T value);
}
