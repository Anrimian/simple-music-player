package com.github.anrimian.musicplayer.domain.models.composition;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class FullComposition {

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nullable
    private final String album;
    @Nullable
    private final String albumArtist;
    @Nonnull
    private final String fileName;

    private final long duration;
    private final long size;
    private final long id;

    @Nullable
    private final Long storageId;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    @Nullable
    private final CorruptionType corruptionType;

    @SuppressWarnings("NullableProblems")
    public FullComposition(String artist,
                           String title,
                           String album,
                           String albumArtist,
                           String fileName,
                           long duration,
                           long size,
                           long id,
                           Long storageId,
                           Date dateAdded,
                           Date dateModified,
                           CorruptionType corruptionType) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumArtist = albumArtist;
        this.fileName = fileName;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.storageId = storageId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;
    }

    @Nullable
    public String getAlbumArtist() {
        return albumArtist;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
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

    @Nullable
    public CorruptionType getCorruptionType() {
        return corruptionType;
    }

    @Override
    public String toString() {
        return "Composition{" +
                "\n id=" + id +
                "\n fileName='" + fileName + '\'' +
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

        FullComposition that = (FullComposition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
