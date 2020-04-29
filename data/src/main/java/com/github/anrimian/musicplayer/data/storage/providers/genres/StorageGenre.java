package com.github.anrimian.musicplayer.data.storage.providers.genres;

import androidx.annotation.NonNull;

public class StorageGenre {

    private final long id;
    private final String name;

    public StorageGenre(long id, String name) {
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

        StorageGenre that = (StorageGenre) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageGenre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
