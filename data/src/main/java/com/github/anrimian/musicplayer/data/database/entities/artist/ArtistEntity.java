package com.github.anrimian.musicplayer.data.database.entities.artist;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "artists",
        indices = {
                @Index(value = "name", unique = true)
        })
public class ArtistEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nonnull
    private String name;

    public ArtistEntity(@Nonnull String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

}
