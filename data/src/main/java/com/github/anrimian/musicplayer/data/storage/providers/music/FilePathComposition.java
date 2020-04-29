package com.github.anrimian.musicplayer.data.storage.providers.music;

public class FilePathComposition {

    private final long id;
    private final Long storageId;
    private final String filePath;

    public FilePathComposition(long id, Long storageId, String filePath) {
        this.id = id;
        this.storageId = storageId;
        this.filePath = filePath;
    }

    public long getId() {
        return id;
    }

    public Long getStorageId() {
        return storageId;
    }

    public String getFilePath() {
        return filePath;
    }
}
