package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FileMetadata {

    private final FileKey fileKey;

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nullable
    private final String album;
    @Nullable
    private final String albumArtist;

    private final String[] genres;

    private final long duration;
    private final long size;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    public FileMetadata(FileKey fileKey,
                        @Nullable String artist,
                        @Nullable String title,
                        @Nullable String album,
                        @Nullable String albumArtist,
                        String[] genres,
                        long duration,
                        long size,
                        @Nonnull Date dateAdded,
                        @Nonnull Date dateModified) {
        this.fileKey = fileKey;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.albumArtist = albumArtist;
        this.genres = genres;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public FileKey getFileKey() {
        return fileKey;
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

    @Nullable
    public String getAlbumArtist() {
        return albumArtist;
    }

    public String[] getGenres() {
        return genres;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileMetadata that = (FileMetadata) o;

        return fileKey.equals(that.fileKey);
    }

    @Override
    public int hashCode() {
        return fileKey.hashCode();
    }
}
