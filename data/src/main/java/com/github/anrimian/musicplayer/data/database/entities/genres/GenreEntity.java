package com.github.anrimian.musicplayer.data.database.entities.genres;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "genres",
        indices = {
                @Index(value = "name", unique = true)
        })
public class GenreEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long storageId;

    @Nonnull
    private String name;

    public GenreEntity(@Nullable Long storageId, @Nonnull String name) {
        this.storageId = storageId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }
}
