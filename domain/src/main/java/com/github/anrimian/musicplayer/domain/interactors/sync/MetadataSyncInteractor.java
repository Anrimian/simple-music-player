package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.LocalFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RunningSyncState;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.exceptions.TooHighRemoteRepositoryVersion;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;

import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final LibraryRepository libraryRepository;
    private final Scheduler scheduler;//single thread pool scheduler

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository,
                                  LibraryRepository libraryRepository,
                                  Scheduler scheduler) {
        this.metadataVersion = metadataVersion;
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
        this.libraryRepository = libraryRepository;
        this.scheduler = scheduler;
    }

    public void runSync() {
        //filter possible often calls?
        Observable.fromIterable(syncSettingsRepository.getEnabledRemoteRepositories())
                .flatMapCompletable(this::runSyncFor)
                .doOnError(this::onSyncError)
                .onErrorComplete()
                .doFinally(() -> syncStateSubject.onNext(new RunningSyncState.Idle()))
                .subscribeOn(scheduler)
                .subscribe();
    }

    public Observable<RunningSyncState> getRunningSyncStateObservable() {
        return syncStateSubject;
    }

    public Observable<RemoteRepositoryType> getCurrentSyncingRepositoryObservable() {
        return currentSyncingRepositorySubject;
    }

    private void onSyncError(Throwable throwable) {
        syncStateSubject.onNext(new RunningSyncState.Error(throwable));
    }

    private Completable runSyncFor(RemoteRepositoryType repositoryType) {
        return Completable.fromAction(() -> {
            RemoteRepository remoteRepository = remoteStoragesRepository.getRemoteRepository(repositoryType);
            runSyncForRepository(remoteRepository, repositoryType);
        });
    }

    //on error stop sync
    private void runSyncForRepository(RemoteRepository remoteRepository, RemoteRepositoryType repositoryType) {
        //get metadata from remote
        syncStateSubject.onNext(new RunningSyncState.GetRemoteMetadata(repositoryType));
        RemoteFilesMetadata remoteMetadata = remoteRepository.getMetadata();

        //check metadata version
        if (remoteMetadata.getVersion() > metadataVersion) {
            remoteStoragesRepository.setEnabledState(repositoryType, DISABLED_VERSION_TOO_HIGH);
            throw new TooHighRemoteRepositoryVersion();
        }

        Map<FileKey, FileMetadata> remoteFiles = remoteMetadata.getFiles();
        Set<FileKey> remoteRemovedFiles = remoteMetadata.getRemovedFiles();

        //get remote real file list
        syncStateSubject.onNext(new RunningSyncState.GetRemoteFileTable(repositoryType));
        Set<FileKey> remoteRealFiles = remoteRepository.getRealFileList();

        //get metadata from local
        syncStateSubject.onNext(new RunningSyncState.CollectLocalFileInfo());
        LocalFilesMetadata localFilesMetadata = libraryRepository.getLocalFilesMetadata();
        Map<FileKey, FileMetadata> localFiles = localFilesMetadata.getLocalFiles();
        Set<FileKey> localRealFiles = localFilesMetadata.getRealFilesList();
        Set<FileKey> localRemovedFiles = localFilesMetadata.getRemovedFiles();

        //calculate changes
//        FileStructMerger.mergeFilesMap();

        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks
    }

}
