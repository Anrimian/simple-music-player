package com.github.anrimian.musicplayer.data.storage.providers.genres;

import androidx.annotation.NonNull;

public class StorageGenreItem {

    private final long id;
    private final long audioId;

    public StorageGenreItem(long id, long audioId) {
        this.id = id;
        this.audioId = audioId;
    }

    public long getId() {
        return id;
    }

    public long getAudioId() {
        return audioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageGenreItem that = (StorageGenreItem) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageGenreItem{" +
                "id=" + id +
                ", audioId=" + audioId +
                '}';
    }
}
