package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.AudioFocusEvent;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.IDLE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.LOADING;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSE;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSED_EXTERNALLY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PAUSED_PREPARE_ERROR;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.PLAY;
import static com.github.anrimian.musicplayer.domain.models.player.PlayerState.STOP;
import static io.reactivex.subjects.BehaviorSubject.createDefault;

public class PlayerInteractor {

    private final MusicPlayerController musicPlayerController;
    private final SystemMusicController systemMusicController;
    private final SystemServiceController systemServiceController;
    private final SettingsRepository settingsRepository;

    private final PublishSubject<PlayerEvent> playerEventsSubject = PublishSubject.create();
    private final BehaviorSubject<PlayerState> playerStateSubject = createDefault(IDLE);
    private final BehaviorSubject<Optional<CompositionSource>> currentSourceSubject = BehaviorSubject.create();

    private final CompositeDisposable systemEventsDisposable = new CompositeDisposable();
    private final CompositeDisposable playerDisposable = new CompositeDisposable();

    @Nullable
    private CompositionSource currentSource;

    public PlayerInteractor(MusicPlayerController musicPlayerController,
                            SettingsRepository settingsRepository,
                            SystemMusicController systemMusicController,
                            SystemServiceController systemServiceController) {
        this.musicPlayerController = musicPlayerController;
        this.systemMusicController = systemMusicController;
        this.settingsRepository = settingsRepository;
        this.systemServiceController = systemServiceController;

        playerDisposable.add(musicPlayerController.getEventsObservable()
                .subscribe(this::onMusicPlayerEventReceived));
        playerDisposable.add(systemMusicController.getVolumeObservable()
                .subscribe(this::onVolumeChanged));
    }

    void startPlaying(CompositionSource compositionSource) {
        prepareToPlay(compositionSource);
        play();
    }

    void prepareToPlay(CompositionSource compositionSource) {
        this.currentSource = compositionSource;
        currentSourceSubject.onNext(new Optional<>(currentSource));
        musicPlayerController.prepareToPlay(compositionSource);
    }

    public void updateSource(CompositionSource source) {
        this.currentSource = source;
        currentSourceSubject.onNext(new Optional<>(currentSource));
    }

    public void play() {
        if (playerStateSubject.getValue() == PLAY) {
            return;
        }
        if (playerStateSubject.getValue() == PAUSED_PREPARE_ERROR && currentSource != null) {
            musicPlayerController.prepareToPlay(currentSource);
        }

        systemEventsDisposable.clear();
        Observable<AudioFocusEvent> audioFocusObservable = systemMusicController.requestAudioFocus();
        if (audioFocusObservable != null) {
            if (playerStateSubject.getValue() != LOADING) {
                musicPlayerController.resume();
                playerStateSubject.onNext(PLAY);
            }
            systemServiceController.startMusicService();

            systemEventsDisposable.add(audioFocusObservable.subscribe(this::onAudioFocusChanged));
            systemEventsDisposable.add(systemMusicController.getAudioBecomingNoisyObservable()
                    .subscribe(this::onAudioBecomingNoisy));
        }
    }

    public void playOrPause() {
        if (playerStateSubject.getValue() == PLAY) {
            pause();
        } else {
            play();
        }
    }

    public void stop() {
        musicPlayerController.stop();
        playerStateSubject.onNext(STOP);
        systemEventsDisposable.clear();
    }

    public void pause() {
        musicPlayerController.pause();
        playerStateSubject.onNext(PAUSE);
        systemEventsDisposable.clear();
    }

    public void onSeekStarted() {
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.pause();
        }
    }

    public void onSeekFinished(long position) {
        if (playerStateSubject.getValue() == PLAY) {
            musicPlayerController.resume();
        }
        musicPlayerController.seekTo(position);
    }

    public Observable<Long> getTrackPositionObservable() {
        return musicPlayerController.getTrackPositionObservable();
    }

    public Observable<PlayerState> getPlayerStateObservable() {
        return playerStateSubject.map(PlayerState::toBaseState)
                .filter(state -> state != LOADING)
                .distinctUntilChanged();
    }

    public PlayerState getPlayerState() {
        return playerStateSubject.getValue();
    }

    public long getTrackPosition() {
        return musicPlayerController.getTrackPosition();
    }

    public Observable<PlayerEvent> getPlayerEventsObservable() {
        return playerEventsSubject;
    }

    public void setInLoadingState() {
        playerStateSubject.onNext(LOADING);
    }

    public Observable<Optional<CompositionSource>> getCurrentSourceObservable() {
        return currentSourceSubject;
    }

    @Nullable
    public CompositionSource getCurrentSource() {
        return currentSource;
    }

    private void onMusicPlayerEventReceived(PlayerEvent playerEvent) {
        if (playerEvent instanceof PreparedEvent) {
            onCompositionPrepared();
        } else if (playerEvent instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) playerEvent;
            handleErrorWithComposition(errorEvent.getErrorType());
        }
        playerEventsSubject.onNext(playerEvent);
    }

    private void onVolumeChanged(int volume) {
        if (playerStateSubject.getValue() == PLAY && volume == 0) {
            pause();
        }
    }

    private void onCompositionPrepared() {
        PlayerState state = playerStateSubject.getValue();
        if (state == LOADING) {
            playerStateSubject.onNext(PLAY);
        }
        if (state == PLAY || state == LOADING) {
            musicPlayerController.resume();
        }
    }

    private void handleErrorWithComposition(ErrorType errorType) {
        if (errorType == ErrorType.IGNORED) {
            musicPlayerController.pause();
            playerStateSubject.onNext(PAUSED_PREPARE_ERROR);
            systemEventsDisposable.clear();
        }
    }

    private void onAudioFocusChanged(AudioFocusEvent event) {
        switch (event) {
            case GAIN: {
                musicPlayerController.setVolume(1f);
                if (playerStateSubject.getValue() == PAUSED_EXTERNALLY) {
                    playerStateSubject.onNext(PLAY);
                    musicPlayerController.resume();
                    systemServiceController.startMusicService();
                }
                break;
            }
            case LOSS_SHORTLY: {
                if (playerStateSubject.getValue() == PLAY
                        && settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled()) {
                    musicPlayerController.setVolume(0.5f);
                }
                break;
            }
            case LOSS: {
                if (playerStateSubject.getValue() == PLAY) {
                    musicPlayerController.pause();
                    playerStateSubject.onNext(PAUSED_EXTERNALLY);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void onAudioBecomingNoisy(Object o) {
        musicPlayerController.pause();
        playerStateSubject.onNext(PAUSE);
    }
}
