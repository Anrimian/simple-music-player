package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Map;
import java.util.Set;

public class LocalFilesMetadata {

    private final Map<FileKey, FileMetadata> localFiles;
    private final Set<FileKey> realFilesList;
    private final Set<FileKey> removedFiles;

    public LocalFilesMetadata(Map<FileKey, FileMetadata> localFiles,
                              Set<FileKey> realFilesList,
                              Set<FileKey> removedFiles) {
        this.localFiles = localFiles;
        this.realFilesList = realFilesList;
        this.removedFiles = removedFiles;
    }

    public Set<FileKey> getRemovedFiles() {
        return removedFiles;
    }

    public Map<FileKey, FileMetadata> getLocalFiles() {
        return localFiles;
    }

    public Set<FileKey> getRealFilesList() {
        return realFilesList;
    }
}
