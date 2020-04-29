package com.github.anrimian.musicplayer.data.database.entities.composition;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "compositions",
        foreignKeys = {
                @ForeignKey(entity = ArtistEntity.class,
                        parentColumns = "id",
                        childColumns = "artistId"),
                @ForeignKey(entity = AlbumEntity.class,
                        parentColumns = "id",
                        childColumns = "albumId"),
                @ForeignKey(entity = FolderEntity.class,
                        parentColumns = "id",
                        childColumns = "folderId")
        },
        indices = {
                @Index("artistId"),
                @Index("albumId"),
                @Index("folderId")
        }
)
public class CompositionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long artistId;

    @Nullable
    private Long albumId;

    @Nullable
    private Long folderId;

    @Nullable
    private Long storageId;

    @Nullable
    private String title;

    @Nonnull
    private String fileName;

    @Nonnull
    private String filePath;

    private long duration;
    private long size;

    @Nonnull
    private Date dateAdded;
    @Nonnull
    private Date dateModified;

    @Nullable
    private CorruptionType corruptionType;

    public CompositionEntity(@Nullable Long artistId,
                             @Nullable Long albumId,
                             @Nullable Long folderId,
                             @Nullable String title,
                             @Nonnull String fileName,
                             @Nonnull String filePath,
                             long duration,
                             long size,
                             @Nullable Long storageId,
                             @Nonnull Date dateAdded,
                             @Nonnull Date dateModified,
                             @Nullable CorruptionType corruptionType) {
        this.artistId = artistId;
        this.albumId = albumId;
        this.folderId = folderId;
        this.storageId = storageId;
        this.title = title;
        this.fileName = fileName;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;

        Objects.requireNonNull(fileName);
    }

    @Nullable
    public Long getFolderId() {
        return folderId;
    }

    @Nullable
    public Long getAlbumId() {
        return albumId;
    }

    @Nullable
    public Long getArtistId() {
        return artistId;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    @Nonnull
    public String getFilePath() {
        return filePath;
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

    @Nullable
    public CorruptionType getCorruptionType() {
        return corruptionType;
    }
}
