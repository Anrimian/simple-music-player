package com.github.anrimian.musicplayer.data.controllers.music;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.controllers.music.players.AndroidMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.AppMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.CompositeMediaPlayer;
import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;

/**
 * Created on 10.11.2017.
 */

public class MusicPlayerControllerImpl implements MusicPlayerController {

    private final AppMediaPlayer mediaPlayer;
    private final UiStateRepository uiStateRepository;

    @Nullable
    CompositionSource currentSource;

    public MusicPlayerControllerImpl(UiStateRepository uiStateRepository,
                                     Context context,
                                     CompositionSourceProvider sourceRepository,
                                     Scheduler scheduler,
                                     PlayerErrorParser playerErrorParser,
                                     Analytics analytics,
                                     EqualizerController equalizerController) {
        this.uiStateRepository = uiStateRepository;
        Function<AppMediaPlayer> exoMediaPlayer = () -> new ExoMediaPlayer(context, sourceRepository, scheduler, playerErrorParser, equalizerController);
        Function<AppMediaPlayer> androidMediaPlayer = () -> new AndroidMediaPlayer(context, scheduler, sourceRepository, playerErrorParser, analytics, equalizerController);
        mediaPlayer = new CompositeMediaPlayer(exoMediaPlayer, androidMediaPlayer);

//        mediaPlayer = new AndroidMediaPlayer(context, scheduler, sourceRepository, playerErrorParser, analytics, equalizerController);
//        mediaPlayer = new ExoMediaPlayer(context, sourceRepository, scheduler, playerErrorParser, equalizerController);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return mediaPlayer.getEventsObservable();
    }

    @Override
    public void prepareToPlay(CompositionSource source) {
        long trackPosition = getStartTrackPosition(source);
        currentSource = source;
        mediaPlayer.prepareToPlay(source, trackPosition);
    }

    @Override
    public void stop() {
        mediaPlayer.stop();
        saveTrackPosition(0);
    }

    @Override
    public void pause() {
        saveTrackPosition(mediaPlayer.getTrackPosition());
        mediaPlayer.pause();
    }

    @Override
    public void seekTo(long position) {
        mediaPlayer.seekTo(position);
        saveTrackPosition(position);
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

    private void saveTrackPosition(long position) {
        if (currentSource instanceof LibraryCompositionSource) {
            uiStateRepository.setTrackPosition(position);
        }
    }

    private long getStartTrackPosition(CompositionSource source) {
        if (source instanceof LibraryCompositionSource) {
            return ((LibraryCompositionSource) source).getTrackPosition();
        }
        return 0;
    }
}
