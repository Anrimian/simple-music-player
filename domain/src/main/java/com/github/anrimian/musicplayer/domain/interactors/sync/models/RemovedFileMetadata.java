package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;

public class RemovedFileMetadata {

    private final FileKey fileKey;
    private final Date addDate;

    public RemovedFileMetadata(FileKey fileKey, Date addDate) {
        this.fileKey = fileKey;
        this.addDate = addDate;
    }

    public FileKey getFileKey() {
        return fileKey;
    }

    public Date getAddDate() {
        return addDate;
    }

    @Override
    public String toString() {
        return "RemovedFileMetadata{" +
                "fileKey=" + fileKey +
                ", addDate=" + addDate +
                '}';
    }
}
