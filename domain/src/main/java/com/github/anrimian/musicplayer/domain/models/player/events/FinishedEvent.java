package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;

public class FinishedEvent implements PlayerEvent {

    @Nonnull
    private final Composition composition;

    public FinishedEvent(@Nonnull Composition composition) {
        this.composition = composition;
    }

    @Nonnull
    public Composition getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "FinishedEvent{" +
                "composition=" + composition +
                '}';
    }
}
