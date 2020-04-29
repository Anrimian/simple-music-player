package com.github.anrimian.musicplayer.domain.models.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionFileSource implements FileSource {

    private final Composition composition;

    public CompositionFileSource(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionFileSource that = (CompositionFileSource) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }
}
