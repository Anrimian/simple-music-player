package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;

public class MusicServiceInteractor {

    private final SettingsRepository settingsRepository;

    public MusicServiceInteractor(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Observable<MusicNotificationSetting> getNotificationSettingObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                getColoredNotificationEnabledObservable(),
                getCoversOnLockScreenEnabledObservable(),
                this::mapToSettingModel);
    }

    private Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return Observable.combineLatest(settingsRepository.getCoversEnabledObservable(),
                settingsRepository.getCoversInNotificationEnabledObservable(),
                (coversEnabled, coversInNotification) -> coversEnabled && coversInNotification);
    }

    private Observable<Boolean> getColoredNotificationEnabledObservable() {
        return Observable.combineLatest(getCoversInNotificationEnabledObservable(),
                settingsRepository.getColoredNotificationEnabledObservable(),
                (coversInNotification, coloredNotification) -> coversInNotification && coloredNotification);
    }

    private Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return Observable.combineLatest(settingsRepository.getCoversInNotificationEnabledObservable(),
                settingsRepository.getCoversOnLockScreenEnabledObservable(),
                (coversInNotification, coversOnLockScreen) -> coversInNotification && coversOnLockScreen);
    }

    private MusicNotificationSetting mapToSettingModel(boolean notificationCovers,
                                                       boolean coloredNotification,
                                                       boolean coversOnLockScreen) {
        return new MusicNotificationSetting(notificationCovers,
                coloredNotification,
                coversOnLockScreen);
    }
}
