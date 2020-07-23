package com.github.anrimian.musicplayer.ui.settings.player;

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpPresenter;


public class PlayerSettingsPresenter extends MvpPresenter<PlayerSettingsView> {

    private final PlayerSettingsInteractor interactor;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    public PlayerSettingsPresenter(PlayerSettingsInteractor interactor, Scheduler uiScheduler) {
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(
                interactor.isDecreaseVolumeOnAudioFocusLossEnabled()
        );

        subscribeOnSelectedEqualizer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onDecreaseVolumeOnAudioFocusLossChecked(boolean checked) {
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(checked);
        interactor.setDecreaseVolumeOnAudioFocusLossEnabled(checked);
    }

    private void subscribeOnSelectedEqualizer() {
        presenterDisposable.add(interactor.getSelectedEqualizerTypeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showSelectedEqualizerType));
    }
}
