package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class RemoteFilesMetadata {

    private final int version;
    private final Date modifyTime;

    private final Map<FileKey, FileMetadata> files;
    private final Set<FileKey> removedFiles;

    public RemoteFilesMetadata(int version,
                               Date modifyTime,
                               Map<FileKey, FileMetadata> files,
                               Set<FileKey> removedFiles) {
        this.version = version;
        this.modifyTime = modifyTime;
        this.files = files;
        this.removedFiles = removedFiles;
    }

    public Map<FileKey, FileMetadata> getFiles() {
        return files;
    }

    public Set<FileKey> getRemovedFiles() {
        return removedFiles;
    }

    public int getVersion() {
        return version;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
