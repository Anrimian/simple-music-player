package com.github.anrimian.musicplayer.data.controllers.music.players;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.utils.functions.Function;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class CompositeMediaPlayer implements AppMediaPlayer {

    private final Function<AppMediaPlayer>[] mediaPlayers;
    private final int startPlayerIndex = 0;

    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();
    private final PublishSubject<Long> trackPositionSubject = PublishSubject.create();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    private AppMediaPlayer currentPlayer;
    private int currentPlayerIndex;

    private CompositionSource currentComposition;
    private long currentTrackPosition;

    @SafeVarargs
    public CompositeMediaPlayer(Function<AppMediaPlayer>... mediaPlayers) {
        this.mediaPlayers = mediaPlayers;

        setPlayer(startPlayerIndex);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition, long startPosition) {
        currentComposition = composition;
        currentTrackPosition = startPosition;

        if (currentPlayerIndex != startPlayerIndex) {
            setPlayer(startPlayerIndex);
        }
        currentPlayer.prepareToPlay(composition, startPosition);
    }

    @Override
    public void stop() {
        currentPlayer.stop();
    }

    @Override
    public void resume() {
        currentPlayer.resume();
    }

    @Override
    public void pause() {
        currentPlayer.pause();
    }

    @Override
    public void seekTo(long position) {
        currentPlayer.seekTo(position);
    }

    @Override
    public void setVolume(float volume) {
        currentPlayer.setVolume(volume);
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        return currentPlayer.getTrackPosition();
    }

    @Override
    public void release() {
        currentPlayer.release();
    }

    private void setPlayer(int index) {
        currentPlayerIndex = index;
        if (currentPlayer != null) {
            currentPlayer.release();
        }
        currentPlayer = mediaPlayers[index].call();

        playerDisposable.clear();
        playerDisposable.add(currentPlayer.getEventsObservable()
                .flatMap(this::onPlayerEventReceived)
                .subscribe(playerEventSubject::onNext)
        );
        playerDisposable.add(currentPlayer.getTrackPositionObservable()
                .subscribe(this::onTrackPositionReceived)
        );
    }

    private void onTrackPositionReceived(long position) {
        this.currentTrackPosition = position;
        trackPositionSubject.onNext(position);
    }

    private Observable<PlayerEvent> onPlayerEventReceived(PlayerEvent event) {
        return Observable.create(emitter -> {
            // if error event, switch to another player and consume event
            if (event instanceof ErrorEvent) {
                ErrorType errorType = ((ErrorEvent) event).getErrorType();
                if (errorType == ErrorType.UNSUPPORTED || errorType == ErrorType.NOT_FOUND) {
                    int newPlayerIndex = currentPlayerIndex + 1;
                    //don't switch player when we reached end of available players
                    if (newPlayerIndex >= 0 && newPlayerIndex < mediaPlayers.length) {
                        setPlayer(newPlayerIndex);
                        currentPlayer.prepareToPlay(currentComposition, currentTrackPosition);
                        return;
                    }
                }
            }
            emitter.onNext(event);
        });
    }
}
