package com.github.anrimian.musicplayer.ui.common.images.models;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;

public class UriCompositionImage {

    private final UriCompositionSource source;

    public UriCompositionImage(UriCompositionSource source) {
        this.source = source;
    }

    public UriCompositionSource getSource() {
        return source;
    }
}
