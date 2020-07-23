package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.data.utils.rx.RxUtils;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;

public class AndroidMediaPlayer implements AppMediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Context context;
    private final Scheduler scheduler;
    private final CompositionSourceProvider sourceRepository;
    private final PlayerErrorParser playerErrorParser;
    private final Analytics analytics;
    private final EqualizerController equalizerController;

    private final MediaPlayer mediaPlayer;

    @Nullable
    private Disposable trackPositionDisposable;

    @Nullable
    private Disposable preparationDisposable;

    @Nullable
    private CompositionSource currentComposition;

    private boolean isSourcePrepared = false;
    private boolean playWhenReady = false;
    private boolean isPlaying = false;

    //problem with error case(file not found), multiple error events
    public AndroidMediaPlayer(Context context,
                              Scheduler scheduler,
                              CompositionSourceProvider sourceRepository,
                              PlayerErrorParser playerErrorParser,
                              Analytics analytics,
                              EqualizerController equalizerController) {
        this.context = context;
        this.scheduler = scheduler;
        this.sourceRepository = sourceRepository;
        this.playerErrorParser = playerErrorParser;
        this.analytics = analytics;
        this.equalizerController = equalizerController;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (currentComposition != null) {
                playerEventSubject.onNext(new FinishedEvent(currentComposition));
            }
        });
        mediaPlayer.setOnErrorListener((mediaPlayer, what, extra) -> {
            sendErrorEvent(what, extra);
            return false;
        });
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition, long startPosition) {
        this.currentComposition = composition;
        RxUtils.dispose(preparationDisposable);
        preparationDisposable = Single.fromCallable(() -> composition)
                .flatMapCompletable(this::prepareMediaSource)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        if (!isPlaying) {
            return;
        }
        if (isSourcePrepared) {
            seekTo(0);
        }
        stopTracingTrackPosition();
        if (isSourcePrepared) {
            pausePlayer();
        }
        isPlaying = false;
        playWhenReady = false;
    }

    @Override
    public void resume() {
        if (isPlaying) {
            return;
        }
        if (isSourcePrepared) {
            start();
        }
        playWhenReady = true;
    }

    @Override
    public void pause() {
        if (!isPlaying) {
            return;
        }
        stopTracingTrackPosition();
        pausePlayer();
        isPlaying = false;
        playWhenReady = false;
    }

    @Override
    public void seekTo(long position) {
        try {
            if (isSourcePrepared) {
                mediaPlayer.seekTo((int) position);
            }
        } catch (IllegalStateException ignored) {}
        trackPositionSubject.onNext(position);
    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        if (currentComposition == null) {
            return 0;
        }
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    @Override
    public void release() {
        equalizerController.detachEqualizer(context);
        stopTracingTrackPosition();
        mediaPlayer.release();
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .map(o -> getTrackPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private void onCompositionPrepared(Throwable throwable, long startPosition) {
        if (throwable == null) {
            seekTo(startPosition);
            playerEventSubject.onNext(new PreparedEvent(currentComposition));
        } else {
            seekTo(0);
            pausePlayer();
            sendErrorEvent(throwable);
        }
    }

    private void sendErrorEvent(int what, int playerError) {
        if (currentComposition != null) {
            playerEventSubject.onNext(new ErrorEvent(
                    getErrorTypeFromPlayerError(what, playerError),
                    currentComposition)
            );
        }
    }

    private ErrorType getErrorTypeFromPlayerError(int what, int playerError) {
        switch (playerError) {
            case MEDIA_ERROR_UNSUPPORTED: {
                return ErrorType.UNSUPPORTED;
            }
            default: {
                analytics.logMessage("unknown player error, what: " + what + ", extra: " + playerError);
                return ErrorType.IGNORED;
            }
        }
    }

    private void sendErrorEvent(Throwable throwable) {
        if (currentComposition != null) {
            playerEventSubject.onNext(new ErrorEvent(
                    playerErrorParser.getErrorType(throwable),
                    currentComposition)
            );
        }
    }

    private Completable prepareMediaSource(CompositionSource composition) {
        return prepareMediaSourceInternal(composition)
                .doOnSubscribe(d -> isSourcePrepared = false)
                .doOnComplete(this::onSourcePrepared);
    }

    private Completable prepareMediaSourceInternal(CompositionSource composition) {
        if (composition instanceof LibraryCompositionSource) {
            long id = ((LibraryCompositionSource) composition).getComposition().getId();
            return sourceRepository.getCompositionFileDescriptorSingle(id)
                    .doOnSuccess(fileDescriptor -> {
                        mediaPlayer.reset();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(fileDescriptor);
                        mediaPlayer.prepare();
                    })
                    .ignoreElement();
        }
        if (composition instanceof UriCompositionSource) {
            return Single.fromCallable(((UriCompositionSource) composition)::getUri)
                    .doOnSuccess(uri -> {
                        mediaPlayer.reset();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(context, uri);
                        mediaPlayer.prepare();
                    })
                    .ignoreElement();

        }
        throw new IllegalArgumentException("unknown composition source");
    }

    private void onSourcePrepared() {
        if (playWhenReady) {
            start();
        }
        isSourcePrepared = true;
    }

    private void pausePlayer() {
        equalizerController.detachEqualizer(context);
        pause(mediaPlayer);
    }

    private void start() {
        equalizerController.attachEqualizer(context, mediaPlayer.getAudioSessionId());
        mediaPlayer.start();
        startTracingTrackPosition();
        isPlaying = true;
    }

    private void pause(MediaPlayer mediaPlayer) {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception ignored) {}
    }

}
