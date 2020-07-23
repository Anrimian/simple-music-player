package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpPresenter;

public class ExternalPlayerPresenter extends MvpPresenter<ExternalPlayerView> {

    private final ExternalPlayerInteractor interactor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private UriCompositionSource compositionSource;

    public ExternalPlayerPresenter(ExternalPlayerInteractor interactor,
                                   Scheduler uiScheduler) {
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showKeepPlayerInBackground(interactor.isExternalPlayerKeepInBackground());

        subscribeOnPlayerStateChanges();
        subscribeOnTrackPositionChanging();
        subscribeOnRepeatMode();
        subscribeOnErrorEvents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!interactor.isExternalPlayerKeepInBackground()) {
            interactor.stop();
        }
        presenterDisposable.dispose();
    }

    void onSourceForPlayingReceived(UriCompositionSource source) {
        compositionSource = source;
        interactor.startPlaying(compositionSource);
        getViewState().displayComposition(source);
    }

    void onPlayPauseClicked() {
        getViewState().showPlayErrorEvent(null);
        interactor.playOrPause();
    }

    void onTrackRewoundTo(int progress) {
        interactor.seekTo(progress);
    }

    void onSeekStart() {
        interactor.onSeekStarted();
    }

    void onSeekStop(int progress) {
        interactor.onSeekFinished(progress);
    }

    void onRepeatModeButtonClicked() {
        interactor.changeExternalPlayerRepeatMode();
    }

    void onKeepPlayerInBackgroundChecked(boolean checked) {
        interactor.setExternalPlayerKeepInBackground(checked);
    }

    private void subscribeOnErrorEvents() {
        presenterDisposable.add(interactor.getErrorEventsObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showPlayErrorEvent));
    }

    private void subscribeOnRepeatMode() {
        presenterDisposable.add(interactor.getExternalPlayerRepeatModeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showRepeatMode));
    }

    private void subscribeOnTrackPositionChanging() {
        presenterDisposable.add(interactor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged));
    }

    private void onTrackPositionChanged(Long currentPosition) {
        if (compositionSource != null) {
            long duration = compositionSource.getDuration();
            getViewState().showTrackState(currentPosition, duration);
        }
    }

    private void subscribeOnPlayerStateChanges() {
        presenterDisposable.add(interactor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }
}
