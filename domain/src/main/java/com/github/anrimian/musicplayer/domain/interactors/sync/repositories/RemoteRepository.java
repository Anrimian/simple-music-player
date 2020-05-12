package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemovedFileMetadata;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RemoteRepository {

    RemoteFilesMetadata getMetadata();

    Set<FileKey> getRealFileList();

    void updateMetadata(RemoteFilesMetadata remoteMetadata,
                        List<FileMetadata> remoteItemsToAdd,
                        List<FileMetadata> remoteItemsToDelete,
                        List<Change<FileMetadata>> remoteChangedItems,
                        List<RemovedFileMetadata> remoteRemovedItemsToAdd,
                        Map<FileKey, RemovedFileMetadata> remoteRemovedItemToDelete);
}
