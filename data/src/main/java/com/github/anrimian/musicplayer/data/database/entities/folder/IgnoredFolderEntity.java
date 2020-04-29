package com.github.anrimian.musicplayer.data.database.entities.folder;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "ignored_folders")
public class IgnoredFolderEntity {

    @PrimaryKey
    @NonNull
    private String relativePath;

    private Date addDate;

    public IgnoredFolderEntity(@NonNull String relativePath, Date addDate) {
        this.relativePath = relativePath;
        this.addDate = addDate;
    }

    @NonNull
    public String getRelativePath() {
        return relativePath;
    }

    public Date getAddDate() {
        return addDate;
    }
}
