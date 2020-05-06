package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Map;
import java.util.Set;

public class LocalFilesMetadata {

    private final Map<FileKey, FileMetadata> localFiles;
    private final Set<FileKey> realFilesList;
    private final Map<FileKey, RemovedFileMetadata> removedFiles;

    public LocalFilesMetadata(Map<FileKey, FileMetadata> localFiles,
                              Set<FileKey> realFilesList,
                              Map<FileKey, RemovedFileMetadata> removedFiles) {
        this.localFiles = localFiles;
        this.realFilesList = realFilesList;
        this.removedFiles = removedFiles;
    }

    public Map<FileKey, RemovedFileMetadata> getRemovedFiles() {
        return removedFiles;
    }

    public Map<FileKey, FileMetadata> getLocalFiles() {
        return localFiles;
    }

    public Set<FileKey> getRealFilesList() {
        return realFilesList;
    }
}
