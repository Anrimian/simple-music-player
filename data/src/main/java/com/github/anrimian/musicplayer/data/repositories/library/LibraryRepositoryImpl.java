package com.github.anrimian.musicplayer.data.repositories.library;

import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.storage.files.StorageFilesDataSource;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 24.10.2017.
 */

public class LibraryRepositoryImpl implements LibraryRepository {

    private final StorageFilesDataSource storageFilesDataSource;
    private final CompositionsDaoWrapper compositionsDao;
    private final ArtistsDaoWrapper artistsDao;
    private final AlbumsDaoWrapper albumsDao;
    private final GenresDaoWrapper genresDao;
    private final FoldersDaoWrapper foldersDao;
    private final SettingsRepository settingsPreferences;
    private final Scheduler scheduler;

    public LibraryRepositoryImpl(StorageFilesDataSource storageFilesDataSource,
                                 CompositionsDaoWrapper compositionsDao,
                                 ArtistsDaoWrapper artistsDao,
                                 AlbumsDaoWrapper albumsDao,
                                 GenresDaoWrapper genresDao,
                                 FoldersDaoWrapper foldersDao,
                                 SettingsRepository settingsPreferences,
                                 Scheduler scheduler) {
        this.storageFilesDataSource = storageFilesDataSource;
        this.compositionsDao = compositionsDao;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
        this.genresDao = genresDao;
        this.foldersDao = foldersDao;
        this.settingsPreferences = settingsPreferences;
        this.scheduler = scheduler;
    }

    @Override
    public Observable<List<Composition>> getAllCompositionsObservable(@Nullable String searchText) {
        return compositionsDao.getAllObservable(settingsPreferences.getCompositionsOrder(), searchText);
    }

    @Override
    public Observable<FullComposition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id);
    }

    @Override
    public Observable<List<Artist>> getArtistsObservable(@Nullable String searchText) {
        return settingsPreferences.getArtistsOrderObservable()
                .switchMap(order -> artistsDao.getAllObservable(order, searchText));
    }

    @Override
    public Observable<List<Album>> getAlbumsObservable(@Nullable String searchText) {
        return settingsPreferences.getAlbumsOrderObservable()
                .switchMap(order -> albumsDao.getAllObservable(order, searchText));
    }

    @Override
    public Observable<List<Genre>> getGenresObservable(@Nullable String searchText) {
        return settingsPreferences.getGenresOrderObservable()
                .switchMap(order -> genresDao.getAllObservable(order, searchText));
    }

    @Override
    public Observable<List<ShortGenre>> getShortGenresInComposition(long compositionId) {
        return genresDao.getShortGenresInComposition(compositionId);
    }

    @Override
    public Observable<List<Composition>> getGenreItemsObservable(long genreId) {
        return genresDao.getCompositionsInGenreObservable(genreId);
    }

    @Override
    public Observable<List<Composition>> getAlbumItemsObservable(long albumId) {
        return albumsDao.getCompositionsInAlbumObservable(albumId);
    }

    @Override
    public Observable<Album> getAlbumObservable(long albumId) {
        return albumsDao.getAlbumObservable(albumId);
    }

    @Override
    public Observable<List<Composition>> getCompositionsByArtist(long artistId) {
        return artistsDao.getCompositionsByArtistObservable(artistId);
    }

    @Override
    public Observable<Artist> getArtistObservable(long artistId) {
        return artistsDao.getArtistObservable(artistId);
    }

    @Override
    public Observable<List<Album>> getAllAlbumsForArtist(long artistId) {
        return albumsDao.getAllAlbumsForArtistObservable(artistId);
    }

    @Override
    public Single<String[]> getAuthorNames() {
        return Single.fromCallable(artistsDao::getAuthorNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getAlbumNames() {
        return Single.fromCallable(albumsDao::getAlbumNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Single<String[]> getGenreNames() {
        return Single.fromCallable(genresDao::getGenreNames)
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                            @Nullable String searchQuery) {
        return settingsPreferences.getFolderOrderObservable()
                .switchMap(order -> foldersDao.getFilesObservable(folderId, order, searchQuery));
    }

    @Override
    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return foldersDao.getFolderObservable(folderId);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return Single.fromCallable(() -> selectAllCompositionsInFolder(folderId))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> getAllCompositionsInFolders(Iterable<FileSource> fileSources) {
        return foldersDao.extractAllCompositionsFromFiles(fileSources, settingsPreferences.getFolderOrder())
                .subscribeOn(scheduler);
    }

    @Override
    public Completable writeErrorAboutComposition(CorruptionType corruptionType, Composition composition) {
        return Completable.fromAction(() -> compositionsDao.setCorruptionType(corruptionType, composition.getId()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable deleteComposition(Composition composition) {
        return Completable.fromAction(() -> {
            storageFilesDataSource.deleteCompositionFile(composition);
            compositionsDao.delete(composition.getId());
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable deleteCompositions(List<Composition> compositions) {
        return Completable.fromAction(() -> {
            storageFilesDataSource.deleteCompositionFiles(compositions);
            compositionsDao.deleteAll(mapList(compositions, Composition::getId));
        }).subscribeOn(scheduler);
    }

    @Override
    public Observable<Genre> getGenreObservable(long genreId) {
        return genresDao.getGenreObservable(genreId);
    }

    @Override
    public Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder) {
        return Single.fromCallable(() -> foldersDao.getFullFolderPath(folder.getId()))
                .map(foldersDao::insert)
                .subscribeOn(scheduler);
    }

    @Override
    public Completable addFolderToIgnore(IgnoredFolder folder) {
        return Completable.fromAction(() -> foldersDao.insert(folder))
                .subscribeOn(scheduler);
    }

    @Override
    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return foldersDao.getIgnoredFoldersObservable();
    }

    @Override
    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return Completable.fromAction(() -> foldersDao.deleteIgnoredFolder(folder))
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return Single.fromCallable(() -> compositionsDao.getAllCompositionsInFolder(folder.getId()))
                .doOnSuccess(compositions -> {
                    storageFilesDataSource.deleteCompositionFiles(compositions);
                    foldersDao.deleteFolder(folder.getId(), compositions);
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Composition>> deleteFolders(List<FileSource> folders) {
        return foldersDao.extractAllCompositionsFromFiles(folders)
                .doOnSuccess(compositions -> {
                    storageFilesDataSource.deleteCompositionFiles(compositions);
                    foldersDao.deleteFolders(extractFolderIds(folders), compositions);
                })
                .subscribeOn(scheduler);
    }

    @Override
    public Single<List<Long>> getAllParentFolders(@Nullable Long currentFolder) {
        return Single.fromCallable(() -> foldersDao.getAllParentFoldersId(currentFolder))
                .subscribeOn(scheduler);
    }

    private List<Long> extractFolderIds(List<FileSource> sources) {
        List<Long> result = new LinkedList<>();
        for (FileSource source : sources) {
            if (source instanceof FolderFileSource) {
                result.add(((FolderFileSource) source).getId());
            }
        }
        return result;
    }

    private List<Composition> selectAllCompositionsInFolder(Long folderId) {
        return foldersDao.getAllCompositionsInFolder(
                folderId,
                settingsPreferences.getFolderOrder());
    }
}
