package com.github.anrimian.musicplayer.domain.models.composition;

import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class Composition {

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nonnull
    private final String fileName;
    @Nullable
    private final String album;

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

    @SuppressWarnings("NullableProblems")//annotations break room annotations processing
    public Composition(String artist,
                       String title,
                       String fileName,
                       String album,
                       long duration,
                       long size,
                       long id,
                       Long storageId,
                       Date dateAdded,
                       Date dateModified,
                       CorruptionType corruptionType) {
        this.artist = artist;
        this.title = title;
        this.fileName = fileName;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.storageId = storageId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;
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

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    @Nullable
    public String getAlbum() {
        return album;
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
                "title='" + title + '\'' +
                ", fileName='" + fileName + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Composition that = (Composition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
