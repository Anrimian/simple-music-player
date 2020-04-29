package com.github.anrimian.musicplayer.data.database.entities.folder;

import androidx.annotation.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageFolder {

    private long id;

    @Nullable
    private Long parentId;

    @Nonnull
    private String name;

    public StorageFolder(long id, @Nullable Long parentId, @Nonnull String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return "StorageFolder{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                '}';
    }
}
