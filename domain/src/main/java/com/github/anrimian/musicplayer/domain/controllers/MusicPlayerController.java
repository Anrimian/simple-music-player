package com.github.anrimian.musicplayer.domain.controllers;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;

/**
 * Created on 10.11.2017.
 */

public interface MusicPlayerController {

    Observable<PlayerEvent> getEventsObservable();

    void prepareToPlay(CompositionSource composition);

    void stop();

    void resume();

    void pause();

    void seekTo(long position);

    void setVolume(float volume);

    Observable<Long> getTrackPositionObservable();

    long getTrackPosition();
}
