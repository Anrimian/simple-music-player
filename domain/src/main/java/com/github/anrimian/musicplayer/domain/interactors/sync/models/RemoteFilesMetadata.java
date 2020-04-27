package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;
import java.util.List;

public class RemoteFilesMetadata {

    private final int version;
    private final Date modifyTime;

    private final List<FileMetadata> files;

    public RemoteFilesMetadata(int version, Date modifyTime, List<FileMetadata> files) {
        this.version = version;
        this.modifyTime = modifyTime;
        this.files = files;
    }

    public List<FileMetadata> getFiles() {
        return files;
    }

    public int getVersion() {
        return version;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
