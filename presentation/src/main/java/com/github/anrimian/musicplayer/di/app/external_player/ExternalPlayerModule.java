package com.github.anrimian.musicplayer.di.app.external_player;

import com.github.anrimian.musicplayer.domain.interactors.player.ExternalPlayerInteractor;
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class ExternalPlayerModule {

    @Provides
    @Nonnull
    ExternalPlayerPresenter externalPlayerPresenter(ExternalPlayerInteractor interactor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new ExternalPlayerPresenter(interactor, uiScheduler);
    }
}
