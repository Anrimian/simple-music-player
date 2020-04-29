package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.playlists.PlayListsRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class PlayListsModule {

    @Provides
    @Nonnull
    PlayListsPresenter playListsPresenter(PlayListsInteractor playListsInteractor,
                                          @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                          ErrorParser errorParser) {
        return new PlayListsPresenter(playListsInteractor, uiSchedule, errorParser);
    }

    @Provides
    @Nonnull
    ChoosePlayListPresenter choosePlayListPresenter(PlayListsInteractor playListsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                                    ErrorParser errorParser) {
        return new ChoosePlayListPresenter(playListsInteractor, uiSchedule, errorParser);
    }

    @Provides
    @Nonnull
    CreatePlayListPresenter createPlayListPresenter(PlayListsInteractor playListsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                                    ErrorParser errorParser) {
        return new CreatePlayListPresenter(playListsInteractor, uiSchedule, errorParser);
    }

    @Provides
    @Nonnull
    PlayListsInteractor playListsInteractor(PlayListsRepository playListsRepository,
                                            UiStateRepository uiStateRepository,
                                            Analytics analytics) {
        return new PlayListsInteractor(playListsRepository, uiStateRepository, analytics);
    }

    @Provides
    @Nonnull
    @Singleton
    PlayListsRepository storagePlayListDataSource(StoragePlayListsProvider playListsProvider,
                                                  PlayListsDaoWrapper playListsDaoWrapper,
                                                  @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new PlayListsRepositoryImpl(playListsProvider,
                playListsDaoWrapper,
                scheduler);
    }

    @Provides
    @Nonnull
    StoragePlayListsProvider storagePlayListsProvider(Context context) {
        return new StoragePlayListsProvider(context);
    }

}
