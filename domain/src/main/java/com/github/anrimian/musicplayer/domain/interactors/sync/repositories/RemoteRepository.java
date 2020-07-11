package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemovedFileMetadata;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface RemoteRepository {

    Single<RemoteFilesMetadata> getMetadata();

    Single<Set<FileKey>> getRealFileList();

    Completable updateMetadata(RemoteFilesMetadata remoteMetadata,
                               List<FileMetadata> remoteItemsToAdd,
                               List<FileMetadata> remoteItemsToDelete,
                               List<Change<FileMetadata>> remoteChangedItems,
                               List<RemovedFileMetadata> remoteRemovedItemsToAdd,
                               Map<FileKey, RemovedFileMetadata> remoteRemovedItemToDelete);

    Completable deleteRemoteFile(FileMetadata fileMetadata);
}
