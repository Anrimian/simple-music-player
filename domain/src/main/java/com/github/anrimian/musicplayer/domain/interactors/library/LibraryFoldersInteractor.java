package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class LibraryFoldersInteractor {

    private final LibraryRepository libraryRepository;
    private final EditorRepository editorRepository;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final PlayListsRepository playListsRepository;
    private final SettingsRepository settingsRepository;
    private final UiStateRepository uiStateRepository;
    private final MediaScannerRepository mediaScannerRepository;

    public LibraryFoldersInteractor(LibraryRepository libraryRepository,
                                    EditorRepository editorRepository,
                                    MusicPlayerInteractor musicPlayerInteractor,
                                    PlayListsRepository playListsRepository,
                                    SettingsRepository settingsRepository,
                                    UiStateRepository uiStateRepository,
                                    MediaScannerRepository mediaScannerRepository) {
        this.libraryRepository = libraryRepository;
        this.editorRepository = editorRepository;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.playListsRepository = playListsRepository;
        this.settingsRepository = settingsRepository;
        this.uiStateRepository = uiStateRepository;
        this.mediaScannerRepository = mediaScannerRepository;
    }

    public Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                            @Nullable String searchQuery) {
        return libraryRepository.getFoldersInFolder(folderId, searchQuery);
    }

    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return libraryRepository.getFolderObservable(folderId);
    }

    public void playAllMusicInFolder(@Nullable Long folderId) {
        libraryRepository.getAllCompositionsInFolder(folderId)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return libraryRepository.getAllCompositionsInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources);
    }

    public void play(List<FileSource> fileSources) {
        libraryRepository.getAllCompositionsInFolders(fileSources)
                .doOnSuccess(musicPlayerInteractor::startPlaying)
                .subscribe();
    }

    public void play(Long folderId, Composition composition) {
        libraryRepository.getAllCompositionsInFolder(folderId)
                .doOnSuccess(compositions -> {
                    int firstPosition = compositions.indexOf(composition);
                    musicPlayerInteractor.startPlaying(compositions, firstPosition);
                })
                .subscribe();
    }

    public Single<List<Composition>> addCompositionsToPlayNext(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(musicPlayerInteractor::addCompositionsToPlayNext);
    }

    public Single<List<Composition>> addCompositionsToEnd(List<FileSource> fileSources) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(musicPlayerInteractor::addCompositionsToEnd);
    }

    public Single<List<Composition>> deleteCompositions(List<FileSource> fileSources) {
        return libraryRepository.deleteFolders(fileSources);
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return libraryRepository.deleteFolder(folder);
    }

    public Single<List<Composition>> addCompositionsToPlayList(Long folderId, PlayList playList) {
        return libraryRepository.getAllCompositionsInFolder(folderId)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return libraryRepository.getAllCompositionsInFolders(fileSources)
                .flatMap(compositions -> playListsRepository.addCompositionsToPlayList(compositions, playList)
                        .toSingleDefault(compositions));
    }

    public void setFolderOrder(Order order) {
        settingsRepository.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return settingsRepository.getFolderOrder();
    }

    public void saveCurrentFolder(@Nullable Long folderId) {
        uiStateRepository.setSelectedFolderScreen(folderId);
    }

    public Single<List<Long>> getCurrentFolderScreens() {
        Long currentFolder = uiStateRepository.getSelectedFolderScreen();
        return libraryRepository.getAllParentFolders(currentFolder);
    }

    public Completable renameFolder(long folderId, String newName) {
        return editorRepository.changeFolderName(folderId, newName);
    }

    public Completable addFolderToIgnore(IgnoredFolder folder) {
        return libraryRepository.addFolderToIgnore(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

    public Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder) {
        return libraryRepository.addFolderToIgnore(folder)
                .flatMap(ignoredFolder -> mediaScannerRepository.runStorageScanner()
                        .toSingleDefault(ignoredFolder)
                );
    }

    public Observable<List<IgnoredFolder>> getIgnoredFoldersObservable() {
        return libraryRepository.getIgnoredFoldersObservable();
    }

    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return libraryRepository.deleteIgnoredFolder(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }

}
