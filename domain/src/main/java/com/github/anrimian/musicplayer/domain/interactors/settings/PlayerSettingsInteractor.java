package com.github.anrimian.musicplayer.domain.interactors.settings;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;

public class PlayerSettingsInteractor {

    private final SettingsRepository settingsRepository;

    public PlayerSettingsInteractor(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public boolean isDecreaseVolumeOnAudioFocusLossEnabled() {
        return settingsRepository.isDecreaseVolumeOnAudioFocusLossEnabled();
    }

    public void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled) {
        settingsRepository.setDecreaseVolumeOnAudioFocusLossEnabled(enabled);
    }

    public Observable<Integer> getSelectedEqualizerTypeObservable() {
        return settingsRepository.getSelectedEqualizerTypeObservable();
    }
}
