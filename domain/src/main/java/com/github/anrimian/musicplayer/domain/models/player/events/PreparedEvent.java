package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;

/**
 * Created on 24.06.2018.
 */
public class PreparedEvent implements PlayerEvent {

    private final CompositionSource composition;

    public PreparedEvent(CompositionSource composition) {
        this.composition = composition;
    }

    public CompositionSource getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "PreparedEvent{" +
                "composition=" + composition +
                '}';
    }
}
