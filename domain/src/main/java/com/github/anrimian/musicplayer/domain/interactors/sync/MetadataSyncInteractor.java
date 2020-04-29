package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RunningSyncState;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository) {
        this.metadataVersion = metadataVersion;
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
    }

    public synchronized void runSync() {
        Observable.fromIterable(syncSettingsRepository.getEnabledRemoteRepositories())
                .flatMapMaybe(this::runSyncFor2)
                //scheduler
                //do on error.. send state in syncStateSubject? Sounds good
                .doFinally(() -> syncStateSubject.onNext(RunningSyncState.IDLE))
                .subscribe();

        for (RemoteRepositoryType repositoryType: syncSettingsRepository.getEnabledRemoteRepositories()) {
            currentSyncingRepositorySubject.onNext(repositoryType);
            runSyncFor(repositoryType);
        }
    }

    public Observable<RunningSyncState> getRunningSyncStateObservable() {
        return syncStateSubject;
    }

    public Observable<RemoteRepositoryType> getCurrentSyncingRepositoryObservable() {
        return currentSyncingRepositorySubject;
    }

    private Maybe<?> runSyncFor2(RemoteRepositoryType repositoryType) {
        return Maybe.create(emitter -> {

        });
    }

    //handle errors?
    private void runSyncFor(RemoteRepositoryType repositoryType) {
        RemoteRepository remoteRepository = remoteStoragesRepository.getRemoteRepository(repositoryType);

        //get metadata from remote
        syncStateSubject.onNext(RunningSyncState.GET_REMOTE_METADATA);
        RemoteFilesMetadata remoteMetadata = remoteRepository.getMetadata();

        //check metadata version
        if (remoteMetadata.getVersion() > metadataVersion) {
            remoteStoragesRepository.setEnabledState(repositoryType, DISABLED_VERSION_TOO_HIGH);
            return;
        }

        List<FileMetadata> remoteFiles = remoteMetadata.getFiles();

        syncStateSubject.onNext(RunningSyncState.GET_REMOTE_FILE_TABLE);
        //get remote real file list
        //get metadata from local
        //get local real file list
        //calculate changes
        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks
    }

}
