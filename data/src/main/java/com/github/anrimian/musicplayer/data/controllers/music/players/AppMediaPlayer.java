package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import io.reactivex.Observable;

public interface AppMediaPlayer {

    Observable<PlayerEvent> getEventsObservable();

    void prepareToPlay(Composition composition, long startPosition);

    void stop();

    void resume();

    void pause();

    void seekTo(long position);

    void setVolume(float volume);

    Observable<Long> getTrackPositionObservable();

    long getTrackPosition();

    void release();
}
