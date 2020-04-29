package com.github.anrimian.musicplayer.data.storage.providers.artist;

import androidx.annotation.NonNull;

public class StorageArtist {

    private final long id;
    private final String name;

    public StorageArtist(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageArtist that = (StorageArtist) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageArtist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
