package com.github.anrimian.musicplayer.data.storage.providers.albums;

import androidx.annotation.NonNull;

public class StorageAlbum {

    private final long id;
    private final String album;

    private final String artist;

    private final int firstYear;
    private final int lastYear;

    public StorageAlbum(long id,
                        String album,
                        String artist,
                        int firstYear,
                        int lastYear) {
        this.id = id;
        this.album = album;
        this.artist = artist;
        this.firstYear = firstYear;
        this.lastYear = lastYear;
    }

    public long getId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getFirstYear() {
        return firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageAlbum that = (StorageAlbum) o;

        if (!album.equals(that.album)) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = album.hashCode();
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageAlbum{" +
                "id=" + id +
                ", album='" + album + '\'' +
                ", artist=" + artist +
                ", firstYear=" + firstYear +
                ", lastYear=" + lastYear +
                '}';
    }
}
