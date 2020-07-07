package com.github.anrimian.musicplayer.data.controllers.music.players;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParser;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.data.utils.exo_player.PlayerEventListener;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.events.ErrorEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.FinishedEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;
import com.github.anrimian.musicplayer.domain.models.player.events.PreparedEvent;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.ContentDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.Loader;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class ExoMediaPlayer implements AppMediaPlayer {

    private final BehaviorSubject<Long> trackPositionSubject = BehaviorSubject.create();
    private final PublishSubject<PlayerEvent> playerEventSubject = PublishSubject.create();

    private final Context context;
    private final CompositionSourceProvider sourceRepository;
    private final Scheduler scheduler;
    private final PlayerErrorParser playerErrorParser;
    private final EqualizerController equalizerController;

    private final SimpleExoPlayer player;

    @Nullable
    private Disposable trackPositionDisposable;

    private CompositionSource currentComposition;

    private boolean isPreparing = false;
    private boolean playAfterPrepare = false;

    public ExoMediaPlayer(Context context,
                          CompositionSourceProvider sourceRepository,
                          Scheduler scheduler,
                          PlayerErrorParser playerErrorParser,
                          EqualizerController equalizerController) {
        this.context = context;
        this.playerErrorParser = playerErrorParser;
        this.sourceRepository = sourceRepository;
        this.scheduler = scheduler;
        //init on main thread?
        player = new SimpleExoPlayer.Builder(context).build();
        this.equalizerController = equalizerController;

        PlayerEventListener playerEventListener = new PlayerEventListener(
                () -> playerEventSubject.onNext(new FinishedEvent(currentComposition)),
                this::sendErrorEvent
        );
        player.addListener(playerEventListener);
    }

    @Override
    public Observable<PlayerEvent> getEventsObservable() {
        return playerEventSubject;
    }

    @Override
    public void prepareToPlay(CompositionSource composition, long startPosition) {
        isPreparing = true;
        this.currentComposition = composition;
        Single.fromCallable(() -> composition)
                .flatMapCompletable(this::prepareMediaSource)
                .doOnEvent(t -> onCompositionPrepared(t, startPosition))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void stop() {
        Completable.fromRunnable(() -> {
            seekTo(0);
            pausePlayer();
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void resume() {
        if (isPreparing) {
            playAfterPrepare = true;
            return;
        }
        startPlayWhenReady();
    }

    @Override
    public void pause() {
        Completable.fromRunnable(() -> {
            pausePlayer();
            stopTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void seekTo(long position) {
        Completable.fromRunnable(() -> {
            player.seekTo(position);
            trackPositionSubject.onNext(position);
        }).subscribeOn(scheduler).subscribe();
    }

    @Override
    public void setVolume(float volume) {
        Completable.fromRunnable(() -> player.setVolume(volume))
                .subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Observable<Long> getTrackPositionObservable() {
        return trackPositionSubject;
    }

    @Override
    public long getTrackPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void release() {
        pausePlayer();
        stopTracingTrackPosition();
        player.release();
    }

    private void startPlayWhenReady() {
        Completable.fromRunnable(() -> {
            equalizerController.attachEqualizer(context, player.getAudioSessionId());
            player.setPlayWhenReady(true);
            startTracingTrackPosition();
        }).subscribeOn(scheduler).subscribe();
    }

    private void onCompositionPrepared(Throwable throwable, long startPosition) {
        isPreparing = false;
        if (throwable == null) {
            seekTo(startPosition);
            playerEventSubject.onNext(new PreparedEvent(currentComposition));
            if (playAfterPrepare) {
                playAfterPrepare = false;
                startPlayWhenReady();
            }
        } else {
            seekTo(0);
            pausePlayer();
            sendErrorEvent(throwable);
        }
    }

    private void pausePlayer() {
        equalizerController.detachEqualizer(context);
        player.setPlayWhenReady(false);
    }

    private void sendErrorEvent(Throwable throwable) {
        if (currentComposition != null) {
            //workaround for prepareError in newest exo player versions
            if (isStrangeLoaderException(throwable)) {
                prepareToPlay(currentComposition, player.getCurrentPosition());
                return;
            }

            playerEventSubject.onNext(new ErrorEvent(
                    playerErrorParser.getErrorType(throwable),
                    currentComposition)
            );
        }
    }

    private void startTracingTrackPosition() {
        stopTracingTrackPosition();
        trackPositionDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(scheduler)
                .map(o -> player.getCurrentPosition())
                .subscribe(trackPositionSubject::onNext);
    }

    private void stopTracingTrackPosition() {
        if (trackPositionDisposable != null) {
            trackPositionDisposable.dispose();
            trackPositionDisposable = null;
        }
    }

    private Completable prepareMediaSource(CompositionSource composition) {
        return getCompositionUri(composition)
                .flatMap(this::createMediaSource)
                .timeout(2, TimeUnit.SECONDS)//read from uri can be freeze for some reason, check
                .observeOn(scheduler)
                .doOnSuccess(player::prepare)
                .ignoreElement();
    }

    private Single<Uri> getCompositionUri(CompositionSource composition) {
        if (composition instanceof LibraryCompositionSource) {
            long id = ((LibraryCompositionSource) composition).getComposition().getId();
            return sourceRepository.getCompositionUri(id);
        }
        if (composition instanceof UriCompositionSource) {
            return Single.fromCallable(((UriCompositionSource) composition)::getUri);
        }
        throw new IllegalArgumentException("unknown composition source");
    }

    private boolean isStrangeLoaderException(Throwable throwable) {
        if (throwable instanceof ExoPlaybackException) {
            Throwable cause = throwable.getCause();
            return cause instanceof Loader.UnexpectedLoaderException;
        }
        return false;
    }

    private Single<MediaSource> createMediaSource(Uri uri) {
        return Single.fromCallable(() -> {
            DataSpec dataSpec = new DataSpec(uri);
            final ContentDataSource dataSource = new ContentDataSource(context);
            dataSource.open(dataSpec);

            DataSource.Factory factory = () -> dataSource;
            return new ProgressiveMediaSource.Factory(factory).createMediaSource(uri);
        });
    }
}
