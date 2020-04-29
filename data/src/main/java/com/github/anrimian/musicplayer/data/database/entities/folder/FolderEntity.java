package com.github.anrimian.musicplayer.data.database.entities.folder;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "folders",
        foreignKeys = {
                @ForeignKey(entity = FolderEntity.class,
                        parentColumns = "id",
                        childColumns = "parentId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index("parentId")
        }
)
public class FolderEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long parentId;

    @Nonnull
    private String name;

    public FolderEntity(@Nullable Long parentId, @Nonnull String name) {
        this.parentId = parentId;
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
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
}
