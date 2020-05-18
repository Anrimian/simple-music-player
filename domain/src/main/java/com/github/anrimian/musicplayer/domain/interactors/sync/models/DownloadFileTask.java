package com.github.anrimian.musicplayer.domain.interactors.sync.models;

public class DownloadFileTask {

    private final FileMetadata fileMetadata;
    private final boolean rescanAfterDownload;

    public DownloadFileTask(FileMetadata fileMetadata) {
        this(fileMetadata, false);
    }

    public DownloadFileTask(FileMetadata fileMetadata, boolean rescanAfterDownload) {
        this.fileMetadata = fileMetadata;
        this.rescanAfterDownload = rescanAfterDownload;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public boolean isRescanAfterDownload() {
        return rescanAfterDownload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadFileTask that = (DownloadFileTask) o;

        if (rescanAfterDownload != that.rescanAfterDownload) return false;
        return fileMetadata.equals(that.fileMetadata);
    }

    @Override
    public int hashCode() {
        int result = fileMetadata.hashCode();
        result = 31 * result + (rescanAfterDownload ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DownloadFileTask{" +
                "fileMetadata=" + fileMetadata +
                ", rescanAfterDownload=" + rescanAfterDownload +
                '}';
    }
}
