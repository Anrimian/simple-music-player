package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;

import java.util.List;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository) {
        this.metadataVersion = metadataVersion;
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
        RemoteFilesMetadata remoteMetadata = remoteRepository.getMetadata();

        //check metadata version
        if (remoteMetadata.getVersion() > metadataVersion) {
            remoteStoragesRepository.setEnabledState(repositoryType, DISABLED_VERSION_TOO_HIGH);
            return;
        }
        List<FileMetadata> remoteFiles = remoteMetadata.getFiles();

        //get remote real file list
        //get metadata from local
        //get local real file list
        //calculate changes
        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks
    }

}
