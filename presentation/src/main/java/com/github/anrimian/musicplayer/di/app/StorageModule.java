package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListsDaoWrapper;
import com.github.anrimian.musicplayer.data.repositories.library.edit.EditorRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.MediaScannerRepositoryImpl;
import com.github.anrimian.musicplayer.data.repositories.scanner.StorageCompositionAnalyzer;
import com.github.anrimian.musicplayer.data.repositories.scanner.StoragePlaylistAnalyzer;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.artist.StorageArtistsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor;
import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.IO_SCHEDULER;

@Module
public class StorageModule {

    @Provides
    @Nonnull
    @Singleton
    StorageMusicProvider storageMusicProvider(Context context, StorageAlbumsProvider albumsProvider) {
        return new StorageMusicProvider(context, albumsProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageArtistsProvider storageArtistProvider(Context context) {
        return new StorageArtistsProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageGenresProvider storageGenresProvider(Context context) {
        return new StorageGenresProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    StorageAlbumsProvider storageAlbumsProvider(Context context) {
        return new StorageAlbumsProvider(context);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionSourceEditor compositionSourceEditor(StorageMusicProvider musicProvider) {
        return new CompositionSourceEditor(musicProvider);
    }

    @Provides
    @Nonnull
    @Singleton
    EditorRepository compositionEditorRepository(CompositionSourceEditor sourceEditor,
                                                 StorageFilesDataSource filesDataSource,
                                                 CompositionsDaoWrapper compositionsDao,
                                                 AlbumsDaoWrapper albumsDao,
                                                 ArtistsDaoWrapper artistsDao,
                                                 GenresDaoWrapper genresDao,
                                                 FoldersDaoWrapper foldersDao,
                                                 StorageMusicProvider storageMusicProvider,
                                                 StorageGenresProvider storageGenresProvider,
                                                 StateRepository stateRepository,
                                                 @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new EditorRepositoryImpl(
                sourceEditor,
                filesDataSource,
                compositionsDao,
                albumsDao,
                artistsDao,
                genresDao,
                foldersDao,
                storageMusicProvider,
                storageGenresProvider,
                stateRepository,
                scheduler);
    }

    @Provides
    @Nonnull
    StorageFilesDataSource storageFilesDataSource(StorageMusicProvider musicProvider) {
        return new StorageFilesDataSource(musicProvider);
    }

    @Provides
    @Nonnull
    EditorInteractor compositionEditorInteractor(EditorRepository editorRepository,
                                                 LibraryRepository musicProviderRepository) {
        return new EditorInteractor(editorRepository, musicProviderRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    MediaScannerRepository mediaScannerRepository(StorageMusicProvider musicProvider,
                                                  StoragePlayListsProvider playListsProvider,
                                                  StorageGenresProvider genresProvider,
                                                  GenresDaoWrapper genresDao,
                                                  StorageCompositionAnalyzer compositionAnalyzer,
                                                  StoragePlaylistAnalyzer storagePlaylistAnalyzer,
                                                  @Named(IO_SCHEDULER) Scheduler scheduler) {
        return new MediaScannerRepositoryImpl(musicProvider,
                playListsProvider,
                genresProvider,
                genresDao,
                compositionAnalyzer,
                storagePlaylistAnalyzer,
                scheduler);
    }

    @Provides
    @Nonnull
    StorageCompositionAnalyzer compositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                                                   FoldersDaoWrapper foldersDaoWrapper,
                                                   StateRepository stateRepository,
                                                   StorageCompositionsInserter compositionsInserter) {
        return new StorageCompositionAnalyzer(compositionsDao,
                foldersDaoWrapper,
                stateRepository,
                compositionsInserter);
    }

    @Provides
    @Nonnull
    StoragePlaylistAnalyzer storagePlaylistAnalyzer(PlayListsDaoWrapper playListsDao,
                                                    StoragePlayListsProvider playListsProvider) {
        return new StoragePlaylistAnalyzer(playListsDao, playListsProvider);
    }

    @Provides
    @Nonnull
    StorageCompositionsInserter compositionsInserter(AppDatabase appDatabase,
                                                     CompositionsDao compositionsDao,
                                                     CompositionsDaoWrapper compositionsDaoWrapper,
                                                     FoldersDao foldersDao,
                                                     ArtistsDao artistsDao,
                                                     AlbumsDao albumsDao) {
        return new StorageCompositionsInserter(appDatabase,
                compositionsDao,
                compositionsDaoWrapper,
                foldersDao,
                artistsDao,
                albumsDao);
    }

}
