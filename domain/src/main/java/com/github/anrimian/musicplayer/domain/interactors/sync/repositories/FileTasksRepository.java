package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface FileTasksRepository {
    //set
    //get

    Completable saveFileTasks(RemoteRepositoryType repositoryType,
                              List<FileMetadata> localFilesToDelete,
                              List<FileMetadata> remoteFilesToDelete,
                              List<FileMetadata> localFilesToUpload,
                              List<DownloadFileTask> remoteFilesToDownload);

    Single<List<RemoteRepositoryType>> getRepositoriesToSync();

    Maybe<FileMetadata> getNextRemoteFileToDelete(RemoteRepositoryType repositoryType);

    Completable removeFileToDelete(FileMetadata fileMetadata, RemoteRepositoryType remoteRepositoryType);
}
