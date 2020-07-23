package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;

import javax.annotation.Nonnull;

public class FinishedEvent implements PlayerEvent {

    @Nonnull
    private final CompositionSource composition;

    public FinishedEvent(@Nonnull CompositionSource composition) {
        this.composition = composition;
    }

    @Nonnull
    public CompositionSource getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "FinishedEvent{" +
                "composition=" + composition +
                '}';
    }
}
