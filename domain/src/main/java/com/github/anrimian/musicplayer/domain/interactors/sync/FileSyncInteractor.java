package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.FileTasksRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

public class FileSyncInteractor {

    private final FileTasksRepository fileTasksRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final Scheduler scheduler;

    private final BehaviorSubject<Boolean> fileSyncStateSubject = BehaviorSubject.createDefault(false);

    public FileSyncInteractor(FileTasksRepository fileTasksRepository,
                              RemoteStoragesRepository remoteStoragesRepository,
                              Scheduler scheduler) {
        this.fileTasksRepository = fileTasksRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
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
                .andThen(deleteLocalFiles(repositoryType, repository))
                .andThen(downloadLocalFiles(repositoryType, repository));
    }

    private Completable deleteRemoteFiles(RemoteRepositoryType repositoryType,
                                          RemoteRepository repository) {
        return fileTasksRepository.getNextRemoteFileToDelete(repositoryType)
                .flatMapCompletable(fileMetadata -> repository.deleteRemoteFile(fileMetadata)
                        .andThen(fileTasksRepository.removeFileToDelete(fileMetadata, repositoryType)))
                .repeat();//not working, it never stops
    }

    private Completable uploadRemoteFiles(RemoteRepositoryType repositoryType,
                                          RemoteRepository repository) {
        return Completable.complete();
    }

    private Completable deleteLocalFiles(RemoteRepositoryType repositoryType,
                                         RemoteRepository repository) {
        return Completable.complete();
    }

    private Completable downloadLocalFiles(RemoteRepositoryType repositoryType,
                                           RemoteRepository repository) {
        return Completable.complete();
    }

    private boolean isFileUploadAllowed() {
        return true;
    }
}
