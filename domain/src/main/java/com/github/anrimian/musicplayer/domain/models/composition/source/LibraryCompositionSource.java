package com.github.anrimian.musicplayer.domain.models.composition.source;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class LibraryCompositionSource implements CompositionSource {
    private final Composition composition;
    private long trackPosition;

    public LibraryCompositionSource(Composition composition, long trackPosition) {
        this.composition = composition;
        this.trackPosition = trackPosition;
    }

    public Composition getComposition() {
        return composition;
    }

    public long getTrackPosition() {
        return trackPosition;
    }

    public void setTrackPosition(long trackPosition) {
        this.trackPosition = trackPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LibraryCompositionSource that = (LibraryCompositionSource) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }

    @Override
    public String toString() {
        return "LibraryCompositionSource{" +
                "composition=" + composition +
                '}';
    }
}
