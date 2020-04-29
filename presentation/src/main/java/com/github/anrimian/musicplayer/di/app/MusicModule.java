package com.github.anrimian.musicplayer.di.app;


import android.content.Context;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.controllers.music.MusicPlayerControllerImpl;
import com.github.anrimian.musicplayer.data.controllers.music.SystemMusicControllerImpl;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.LibraryRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.play_queue.PlayQueueRepositoryImpl;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.controllers.MusicPlayerController;
import com.github.anrimian.musicplayer.domain.controllers.SystemMusicController;
import com.github.anrimian.musicplayer.domain.controllers.SystemServiceController;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 02.11.2017.
 */

@Module
class MusicModule {

    @Provides
    @NonNull
    @Singleton
    MusicPlayerInteractor musicPlayerInteractor(MusicPlayerController musicPlayerController,
                                                SettingsRepository settingsRepository,
                                                SystemMusicController systemMusicController,
                                                SystemServiceController systemServiceController,
                                                PlayQueueRepository playQueueRepository,
                                                LibraryRepository musicProviderRepository,
                                                UiStateRepository uiStateRepository,
                                                Analytics analytics) {
        return new MusicPlayerInteractor(musicPlayerController,
                settingsRepository,
                systemMusicController,
                systemServiceController,
                playQueueRepository,
                musicProviderRepository,
                uiStateRepository,
                analytics);
    }

    @Provides
    @NonNull
    @Singleton
    PlayQueueRepository playQueueRepository(PlayQueueDaoWrapper playQueueDao,
                                            SettingsRepository settingsPreferences,
                                            UiStateRepository uiStateRepository,
                                            @Named(DB_SCHEDULER) Scheduler dbScheduler) {
        return new PlayQueueRepositoryImpl(playQueueDao,
                settingsPreferences,
                uiStateRepository,
                dbScheduler);
    }

    @Provides
    @NonNull
    @Singleton
    SystemMusicController provideSystemMusicController(Context context) {
        return new SystemMusicControllerImpl(context);
    }

    @Provides
    @NonNull
    @Singleton
    MusicPlayerController provideMusicPlayerController(UiStateRepository uiStateRepository,
                                                       Context context,
                                                       CompositionSourceProvider sourceRepository,
                                                       @Named(UI_SCHEDULER) Scheduler scheduler,
                                                       PlayerErrorParser playerErrorParser,
                                                       Analytics analytics) {
        return new MusicPlayerControllerImpl(uiStateRepository,
                context,
                sourceRepository,
                scheduler,
                playerErrorParser,
                analytics);
    }

    @Provides
    @NonNull
    @Singleton
    LibraryRepository musicProviderRepository(StorageFilesDataSource storageFilesDataSource,
                                              CompositionsDaoWrapper compositionsDao,
                                              ArtistsDaoWrapper artistsDao,
                                              AlbumsDaoWrapper albumsDao,
                                              GenresDaoWrapper genresDao,
                                              FoldersDaoWrapper foldersDao,
                                              SettingsRepository settingsPreferences,
                                              @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new LibraryRepositoryImpl(
                storageFilesDataSource,
                compositionsDao,
                artistsDao,
                albumsDao,
                genresDao,
                foldersDao,
                settingsPreferences,
                scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    MusicServiceInteractor musicServiceInteractor(SettingsRepository settingsRepository) {
        return new MusicServiceInteractor(settingsRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    CoverImageLoader coverImageLoader(Context context, ThemeController themeController) {
        return new CoverImageLoader(context, themeController);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionSourceProvider sourceRepository(CompositionsDaoWrapper compositionsDao,
                                               StorageMusicProvider storageMusicProvider,
                                               @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new CompositionSourceProvider(compositionsDao, storageMusicProvider, scheduler);
    }

}
