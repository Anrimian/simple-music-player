package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.FileTasksRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

public class FileSyncInteractor {

    private final FileTasksRepository fileTasksRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final LibraryRepository libraryRepository;
    private final Scheduler scheduler;

    private final BehaviorSubject<Boolean> fileSyncStateSubject = BehaviorSubject.createDefault(false);

    public FileSyncInteractor(FileTasksRepository fileTasksRepository,
                              RemoteStoragesRepository remoteStoragesRepository,
                              LibraryRepository libraryRepository,
                              Scheduler scheduler) {
        this.fileTasksRepository = fileTasksRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
        this.libraryRepository = libraryRepository;
        this.scheduler = scheduler;
    }

    public Completable scheduleFileTasks(RemoteRepositoryType repositoryType,
                                         List<FileMetadata> localFilesToDelete,
                                         List<FileMetadata> remoteFilesToDelete,
                                         List<FileMetadata> localFilesToUpload,
                                         List<DownloadFileTask> remoteFilesToDownload) {
        return fileTasksRepository.saveFileTasks(repositoryType,
                localFilesToDelete,
                remoteFilesToDelete,
                localFilesToUpload,
                remoteFilesToDownload
        ).doOnComplete(this::requestFileSync);
    }

    public void requestFileSync() {
        if (isFileUploadAllowed()) {
            runFileSync();
        }
    }

    public Observable<Boolean> getFileSyncStateObservable() {
        return fileSyncStateSubject;
    }

    private void runFileSync() {
        fileTasksRepository.getRepositoriesToSync()
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(this::runFileSyncFor)
                .doOnSubscribe(d -> fileSyncStateSubject.onNext(true))
                .doOnComplete(() -> fileSyncStateSubject.onNext(false))
                .onErrorComplete()
                .subscribeOn(scheduler)
                .subscribe();
    }

    private Completable runFileSyncFor(RemoteRepositoryType repositoryType) {
        return runFileSyncFor(repositoryType, remoteStoragesRepository.getRemoteRepository(repositoryType));
    }

    private Completable runFileSyncFor(RemoteRepositoryType repositoryType,
                                       RemoteRepository repository) {
        return deleteRemoteFiles(repositoryType, repository)
                .andThen(uploadRemoteFiles(repositoryType, repository))
                .andThen(deleteLocalFiles())
                .andThen(downloadLocalFiles(repositoryType, repository));
    }

    private Completable deleteRemoteFiles(RemoteRepositoryType repositoryType,
                                          RemoteRepository repository) {
        Maybe<FileMetadata> maybe = fileTasksRepository.getNextRemoteFileToDelete(repositoryType);

        return RxUtils.repeatUntilComplete(maybe)
                .flatMapCompletable(fileMetadata -> repository.deleteRemoteFile(fileMetadata)
                        .andThen(fileTasksRepository.removeRemoteFileToDelete(fileMetadata, repositoryType))
                );
    }

    private Completable uploadRemoteFiles(RemoteRepositoryType repositoryType,
                                          RemoteRepository repository) {
        Maybe<FileMetadata> maybe = fileTasksRepository.getNextLocalFileToUpload(repositoryType);

        return RxUtils.repeatUntilComplete(maybe)
                .flatMapCompletable(fileMetadata -> repository.uploadFile(fileMetadata)
                        .andThen(fileTasksRepository.removeLocalFileToUpload(fileMetadata, repositoryType))
                );
    }

    private Completable deleteLocalFiles() {
        Maybe<FileMetadata> maybe = fileTasksRepository.getNextLocalFileToDelete();

        return RxUtils.repeatUntilComplete(maybe)
                .flatMapCompletable(fileMetadata -> libraryRepository.deleteFile(fileMetadata)
                        .andThen(fileTasksRepository.removeLocalFileToDelete(fileMetadata))
                );
    }

    private Completable downloadLocalFiles(RemoteRepositoryType repositoryType,
                                           RemoteRepository repository) {
        Maybe<DownloadFileTask> maybe = fileTasksRepository.getNextRemoteFileToDownload(repositoryType);

        return RxUtils.repeatUntilComplete(maybe)
                .flatMapCompletable(fileMetadata -> repository.downloadFile(fileMetadata.getFileMetadata())
                        .andThen(fileTasksRepository.removeRemoteFileToDownload(fileMetadata.getFileMetadata(), repositoryType))
                );
    }

    private boolean isFileUploadAllowed() {
        return true;
    }
}
