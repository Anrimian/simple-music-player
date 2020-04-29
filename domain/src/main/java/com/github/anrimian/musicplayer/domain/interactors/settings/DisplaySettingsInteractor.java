package com.github.anrimian.musicplayer.domain.interactors.settings;

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;

public class DisplaySettingsInteractor {

    private final SettingsRepository settingsRepository;

    public DisplaySettingsInteractor(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Observable<Boolean> getCoversEnabledObservable() {
        return settingsRepository.getCoversEnabledObservable();
    }

    public Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return settingsRepository.getCoversInNotificationEnabledObservable();
    }

    public boolean isCoversEnabled() {
        return settingsRepository.isCoversEnabled();
    }

    public boolean isCoversInNotificationEnabled() {
        return settingsRepository.isCoversInNotificationEnabled();
    }

    public boolean isColoredNotificationEnabled() {
        return settingsRepository.isColoredNotificationEnabled();
    }

    public boolean isCoversOnLockScreenEnabled() {
        return settingsRepository.isCoversOnLockScreenEnabled();
    }

    public void setCoversEnabled(boolean enabled) {
        settingsRepository.setCoversEnabled(enabled);
    }

    public void setCoversInNotificationEnabled(boolean enabled) {
        settingsRepository.setCoversInNotificationEnabled(enabled);
    }

    public void setColoredNotificationEnabled(boolean enabled) {
        settingsRepository.setColoredNotificationEnabled(enabled);
    }

    public void setCoversOnLockScreenEnabled(boolean enabled) {
        settingsRepository.setCoversOnLockScreenEnabled(enabled);
    }
}
