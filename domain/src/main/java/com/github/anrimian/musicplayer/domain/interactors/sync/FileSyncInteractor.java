package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

import java.util.List;

import io.reactivex.Completable;

public class FileSyncInteractor {

    public Completable scheduleFileTasks(RemoteRepositoryType repositoryType,
                                         List<FileMetadata> localFilesToDelete,
                                         List<FileMetadata> remoteFilesToDelete,
                                         List<FileMetadata> localFilesToUpload,
                                         List<DownloadFileTask> remoteFilesToDownload) {
        return Completable.complete();
    }
}
