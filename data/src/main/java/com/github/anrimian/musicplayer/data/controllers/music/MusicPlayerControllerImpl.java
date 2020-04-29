package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.players.AndroidMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.AppMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.CompositeMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final AppMediaPlayer mediaPlayer;
    private final UiStateRepository uiStateRepository;

    public MusicPlayerControllerImpl(UiStateRepository uiStateRepository,
                                     Context context,
                                     CompositionSourceProvider sourceRepository,
                                     Scheduler scheduler,
                                     PlayerErrorParser playerErrorParser,
                                     Analytics analytics) {
        this.uiStateRepository = uiStateRepository;
        Function<AppMediaPlayer> exoMediaPlayer = () -> new ExoMediaPlayer(context, sourceRepository, scheduler, playerErrorParser);
        Function<AppMediaPlayer> androidMediaPlayer = () -> new AndroidMediaPlayer(scheduler, sourceRepository, playerErrorParser, analytics);
        mediaPlayer = new CompositeMediaPlayer(exoMediaPlayer, androidMediaPlayer);

//        mediaPlayer = new AndroidMediaPlayer(scheduler, sourceRepository, playerErrorParser, analytics);
//        mediaPlayer = new ExoMediaPlayer(context, scheduler, playerErrorParser);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return mediaPlayer.getEventsObservable();
    }

    @Override
    public void prepareToPlay(Composition composition, long startPosition) {
        mediaPlayer.prepareToPlay(composition, startPosition);
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        uiStateRepository.setTrackPosition(0);
    }

    @Override
    public void pause() {
        uiStateRepository.setTrackPosition(mediaPlayer.getTrackPosition());
        mediaPlayer.pause();
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo(position);
        uiStateRepository.setTrackPosition(position);
    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume);
    }

    @Override
    public void resume() {
        mediaPlayer.resume();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return mediaPlayer.getTrackPositionObservable();
    }

    @Override
    public long getTrackPosition() {
        return mediaPlayer.getTrackPosition();
    }
}
