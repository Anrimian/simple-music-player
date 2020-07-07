package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.repositories.settings.SettingsRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.PlayerSettingsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsPresenter;
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 21.04.2018.
 */
@Module
public class SettingsModule {

    @Provides
    @Nonnull
    @Singleton
    SettingsRepository provideSettingsRepository(Context context) {
        return new SettingsRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    UiStateRepository provideUiStateRepository(Context context) {
        return new UiStateRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StateRepository uiStateRepository(Context context) {
        return new StateRepositoryImpl(context);
    }

    @Provides
    @Nonnull
    DisplaySettingsInteractor displaySettingsInteractor(SettingsRepository settingsRepository) {
        return new DisplaySettingsInteractor(settingsRepository);
    }

    @Provides
    @Nonnull
    DisplaySettingsPresenter displaySettingsPresenter(DisplaySettingsInteractor displaySettingsInteractor) {
        return new DisplaySettingsPresenter(displaySettingsInteractor);
    }

    @Provides
    @Nonnull
    PlayerSettingsInteractor playerSettingsInteractor(SettingsRepository settingsRepository) {
        return new PlayerSettingsInteractor(settingsRepository);
    }

    @Provides
    @Nonnull
    PlayerSettingsPresenter playerSettingsPresenter(PlayerSettingsInteractor playerSettingsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new PlayerSettingsPresenter(playerSettingsInteractor, uiScheduler);
    }
}
