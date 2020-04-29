package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

/**
 * Created on 24.06.2018.
 */
public class PreparedEvent implements PlayerEvent {

    private final Composition composition;

    public PreparedEvent(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "PreparedEvent{" +
                "composition=" + composition +
                '}';
    }
}
