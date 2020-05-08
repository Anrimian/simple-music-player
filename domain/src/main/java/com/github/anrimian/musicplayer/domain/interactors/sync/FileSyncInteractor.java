package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

import java.util.List;

public class FileSyncInteractor {

    public void scheduleFileTasks(RemoteRepositoryType repositoryType,
                                  List<FileMetadata> localFilesToDelete,
                                  List<FileMetadata> remoteFilesToDelete,
                                  List<FileMetadata> localFilesToUpload,
                                  List<FileMetadata> remoteFilesToDownload) {

    }
}
