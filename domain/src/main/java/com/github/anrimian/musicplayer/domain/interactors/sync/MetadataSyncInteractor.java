package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;

public class MetadataSyncInteractor {

    private static final int CURRENT_METADATA_VERSION = 1;

    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;

    public MetadataSyncInteractor(SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository) {
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
    }

    public synchronized void runSync() {
        for (RemoteRepositoryType repositoryType: syncSettingsRepository.getEnabledRemoteRepositories()) {
            runSyncFor(repositoryType);
        }
    }

    private void runSyncFor(RemoteRepositoryType repositoryType) {
        RemoteRepository remoteRepository = remoteStoragesRepository.getRemoteRepository(repositoryType);

        //get metadata from remote
        FilesMetadata filesMetadata = remoteRepository.getMetadata();

        //check metadata version
        if (filesMetadata.getVersion() > CURRENT_METADATA_VERSION) {
            //stop all sync?
        }

        //get remote real file list
        //get metadata from local
        //get local real file list
        //calculate changes
        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks
    }

}
