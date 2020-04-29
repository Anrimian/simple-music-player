package com.github.anrimian.musicplayer.domain.utils.functions;

public interface Mapper<T, R> {
    R map(T o);
}
