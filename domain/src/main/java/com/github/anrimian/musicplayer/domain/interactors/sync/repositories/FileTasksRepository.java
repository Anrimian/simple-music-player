package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface FileTasksRepository {

    Completable saveFileTasks(RemoteRepositoryType repositoryType,
                              List<FileMetadata> localFilesToDelete,
                              List<FileMetadata> remoteFilesToDelete,
                              List<FileMetadata> localFilesToUpload,
                              List<DownloadFileTask> remoteFilesToDownload);

    Single<List<RemoteRepositoryType>> getRepositoriesToSync();

    Maybe<FileMetadata> getNextRemoteFileToDelete(RemoteRepositoryType repositoryType);

    Maybe<FileMetadata> getNextLocalFileToUpload(RemoteRepositoryType repositoryType);

    Maybe<FileMetadata> getNextLocalFileToDelete();

    Maybe<DownloadFileTask> getNextRemoteFileToDownload(RemoteRepositoryType repositoryType);

    Completable removeRemoteFileToDelete(FileMetadata fileMetadata, RemoteRepositoryType remoteRepositoryType);

    Completable removeLocalFileToUpload(FileMetadata fileMetadata, RemoteRepositoryType remoteRepositoryType);

    Completable removeLocalFileToDelete(FileMetadata fileMetadata);

    Completable removeRemoteFileToDownload(FileMetadata fileMetadata, RemoteRepositoryType remoteRepositoryType);
}
