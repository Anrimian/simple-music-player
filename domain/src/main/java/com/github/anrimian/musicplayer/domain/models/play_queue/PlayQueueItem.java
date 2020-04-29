package com.github.anrimian.musicplayer.domain.models.play_queue;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;

public class PlayQueueItem {

    private final long id;

    @Nonnull
    private final Composition composition;

    public PlayQueueItem(long id, @Nonnull Composition composition) {
        this.id = id;
        this.composition = composition;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public Composition getComposition() {
        return composition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayQueueItem that = (PlayQueueItem) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "PlayQueueItem{" +
                "id=" + id +
                ", composition=" + composition +
                '}';
    }
}
