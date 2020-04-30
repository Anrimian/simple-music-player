package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RunningSyncState;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.exceptions.TooHighRemoteRepositoryVersion;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final Scheduler scheduler;//single thread pool scheduler

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository,
                                  Scheduler scheduler) {
        this.metadataVersion = metadataVersion;
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
        this.scheduler = scheduler;
    }

    public void runSync() {
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

        List<FileMetadata> remoteFiles = remoteMetadata.getFiles();

        syncStateSubject.onNext(new RunningSyncState.GetRemoteFileTable(repositoryType));
        //get remote real file list
        //get metadata from local
        //get local real file list
        //calculate changes
        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks
    }

}
