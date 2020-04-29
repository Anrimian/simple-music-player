package com.github.anrimian.musicplayer.data.storage.providers.music;

import androidx.annotation.NonNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class StorageComposition {

    @Nullable
    private final String artist;
    @Nullable
    private final String albumArtist;
    @Nullable
    private final String title;
    @Nonnull
    private final String fileName;
    @Nullable
    private final String album;
    @Nonnull
    private final String filePath;

    private final long duration;
    private final long size;
    private final long id;
    private final long storageId;

    @Nullable
    private final Long folderId;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    public StorageComposition(@Nullable String artist,
                              @Nullable String albumArtist,
                              @Nullable String title,
                              @Nonnull String fileName,
                              @Nullable String album,
                              @Nonnull String filePath,
                              long duration,
                              long size,
                              long id,
                              long storageId,
                              @Nullable Long folderId,
                              @Nonnull Date dateAdded,
                              @Nonnull Date dateModified) {
        this.artist = artist;
        this.albumArtist = albumArtist;
        this.title = title;
        this.fileName = fileName;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.storageId = storageId;
        this.folderId = folderId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    @Nullable
    public Long getFolderId() {
        return folderId;
    }

    public long getStorageId() {
        return storageId;
    }

    @Nullable
    public String getAlbumArtist() {
        return albumArtist;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nonnull
    public String getFilePath() {
        return filePath;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @NonNull
    @Override
    public String toString() {
        return "Composition{" +
                "\n id=" + id +
                "\n filePath='" + filePath + '\'' +
                "\n duration=" + duration +
                "\n size=" + size +
                "\n dateAdded=" + dateAdded +
                "\n dateModified=" + dateModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageComposition that = (StorageComposition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
