package com.github.anrimian.musicplayer.ui.settings.player;

import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor;

import moxy.MvpPresenter;


public class PlayerSettingsPresenter extends MvpPresenter<PlayerSettingsView> {

    private final PlayerSettingsInteractor interactor;

    public PlayerSettingsPresenter(PlayerSettingsInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(
                interactor.isDecreaseVolumeOnAudioFocusLossEnabled()
        );
    }

    void onDecreaseVolumeOnAudioFocusLossChecked(boolean checked) {
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(checked);
        interactor.setDecreaseVolumeOnAudioFocusLossEnabled(checked);
    }
}
