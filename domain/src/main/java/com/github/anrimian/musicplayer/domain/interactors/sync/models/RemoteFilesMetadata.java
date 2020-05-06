package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;
import java.util.Map;

public class RemoteFilesMetadata {

    private final int version;
    private final Date modifyTime;

    private final Map<FileKey, FileMetadata> files;
    private final Map<FileKey, RemovedFileMetadata> removedFiles;

    public RemoteFilesMetadata(int version,
                               Date modifyTime,
                               Map<FileKey, FileMetadata> files,
                               Map<FileKey, RemovedFileMetadata> removedFiles) {
        this.version = version;
        this.modifyTime = modifyTime;
        this.files = files;
        this.removedFiles = removedFiles;
    }

    public Map<FileKey, FileMetadata> getFiles() {
        return files;
    }

    public Map<FileKey, RemovedFileMetadata> getRemovedFiles() {
        return removedFiles;
    }

    public int getVersion() {
        return version;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
