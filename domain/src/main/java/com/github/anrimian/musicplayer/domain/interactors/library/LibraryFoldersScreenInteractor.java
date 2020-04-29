package com.github.anrimian.musicplayer.domain.interactors.library;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created on 24.10.2017.
 */

public class LibraryFoldersScreenInteractor {

    private final LibraryFoldersInteractor foldersInteractor;
    private final LibraryRepository libraryRepository;
    private final EditorRepository editorRepository;
    private final MediaScannerRepository mediaScannerRepository;


    private final BehaviorSubject<Boolean> moveModeSubject = BehaviorSubject.createDefault(false);
    private final LinkedHashSet<FileSource> filesToCopy = new LinkedHashSet<>();
    private final LinkedHashSet<FileSource> filesToMove = new LinkedHashSet<>();

    @Nullable
    private Long moveFromFolderId;

    public LibraryFoldersScreenInteractor(LibraryFoldersInteractor foldersInteractor,
                                          LibraryRepository libraryRepository,
                                          EditorRepository editorRepository,
                                          MediaScannerRepository mediaScannerRepository) {
        this.foldersInteractor = foldersInteractor;
        this.libraryRepository = libraryRepository;
        this.editorRepository = editorRepository;
        this.mediaScannerRepository = mediaScannerRepository;
    }

    public Observable<List<FileSource>> getFoldersInFolder(@Nullable Long folderId,
                                                            @Nullable String searchQuery) {
        return foldersInteractor.getFoldersInFolder(folderId, searchQuery);
    }

    public Observable<FolderFileSource> getFolderObservable(long folderId) {
        return foldersInteractor.getFolderObservable(folderId);
    }

    public void playAllMusicInFolder(@Nullable Long folderId) {
        foldersInteractor.playAllMusicInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFolder(@Nullable Long folderId) {
        return foldersInteractor.getAllCompositionsInFolder(folderId);
    }

    public Single<List<Composition>> getAllCompositionsInFileSources(List<FileSource> fileSources) {
        return foldersInteractor.getAllCompositionsInFileSources(fileSources);
    }

    public void play(List<FileSource> fileSources) {
        foldersInteractor.play(fileSources);
    }

    public void play(Long folderId, Composition composition) {
        foldersInteractor.play(folderId, composition);
    }

    public Single<List<Composition>> addCompositionsToPlayNext(List<FileSource> fileSources) {
        return foldersInteractor.addCompositionsToPlayNext(fileSources);
    }

    public Single<List<Composition>> addCompositionsToEnd(List<FileSource> fileSources) {
        return foldersInteractor.addCompositionsToEnd(fileSources);
    }

    public Single<List<Composition>> deleteFiles(List<FileSource> fileSources) {
        return foldersInteractor.deleteCompositions(fileSources);
    }

    public Single<List<Composition>> deleteFolder(FolderFileSource folder) {
        return foldersInteractor.deleteFolder(folder);
    }

    public Single<List<Composition>> addCompositionsToPlayList(Long folderId, PlayList playList) {
        return foldersInteractor.addCompositionsToPlayList(folderId, playList);
    }

    public Single<List<Composition>> addCompositionsToPlayList(List<FileSource> fileSources, PlayList playList) {
        return foldersInteractor.addCompositionsToPlayList(fileSources, playList);
    }

    public void setFolderOrder(Order order) {
        foldersInteractor.setFolderOrder(order);
    }

    public Order getFolderOrder() {
        return foldersInteractor.getFolderOrder();
    }

    public void saveCurrentFolder(@Nullable Long folderId) {
        foldersInteractor.saveCurrentFolder(folderId);
    }

    public Single<List<Long>> getCurrentFolderScreens() {
        return foldersInteractor.getCurrentFolderScreens();
    }

    public Completable renameFolder(long folderId, String newName) {
        return foldersInteractor.renameFolder(folderId, newName);
    }

    public void addFilesToMove(@Nullable Long folderId, Collection<FileSource> fileSources) {
        filesToMove.clear();
        filesToMove.addAll(fileSources);
        this.moveFromFolderId = folderId;
        moveModeSubject.onNext(true);
    }

    public void addFilesToCopy(@Nullable Long folderId, Collection<FileSource> fileSources) {
        filesToCopy.clear();
        filesToCopy.addAll(fileSources);
        this.moveFromFolderId = folderId;
        moveModeSubject.onNext(true);
    }

    public void stopMoveMode() {
        filesToCopy.clear();
        filesToMove.clear();
        moveFromFolderId = null;
        moveModeSubject.onNext(false);
    }

    public Completable moveFilesTo(@Nullable Long folderId) {
        Completable completable;
        if (!filesToMove.isEmpty()) {
            completable = editorRepository.moveFiles(filesToMove, moveFromFolderId, folderId);
        } else if (!filesToCopy.isEmpty()) {
            completable = Completable.error(new Exception("not implemented"));
        } else {
            throw new IllegalStateException("unexpected state");
        }
        return completable.doOnComplete(this::stopMoveMode);
    }

    public Completable moveFilesToNewFolder(@Nullable Long folderId, String folderName) {
        Completable completable;
        if (!filesToMove.isEmpty()) {
            completable = editorRepository.moveFilesToNewDirectory(filesToMove,
                    moveFromFolderId,
                    folderId,
                    folderName);
        } else if (!filesToCopy.isEmpty()) {
            completable = Completable.error(new Exception("not implemented"));
        } else {
            throw new IllegalStateException("unexpected state");
        }
        return completable.doOnComplete(this::stopMoveMode);
    }

    public BehaviorSubject<Boolean> getMoveModeObservable() {
        return moveModeSubject;
    }

    public LinkedHashSet<FileSource> getFilesToMove() {
        return filesToMove;
    }

    public Single<IgnoredFolder> addFolderToIgnore(FolderFileSource folder) {
        return foldersInteractor.addFolderToIgnore(folder);
    }

    public Completable deleteIgnoredFolder(IgnoredFolder folder) {
        return libraryRepository.deleteIgnoredFolder(folder)
                .andThen(mediaScannerRepository.runStorageScanner());
    }
}
