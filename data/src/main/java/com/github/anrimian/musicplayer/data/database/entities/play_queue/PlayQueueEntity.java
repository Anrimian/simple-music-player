package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

@Entity(tableName = "play_queue",
        foreignKeys = {
                @ForeignKey(entity = CompositionEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"audioId"},
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index({"audioId"}),
                @Index(value = "position", unique = true),
                @Index(value = "shuffledPosition", unique = true)
        }
)
public class PlayQueueEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long audioId;

    private int position;
    private int shuffledPosition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getShuffledPosition() {
        return shuffledPosition;
    }

    public void setShuffledPosition(int shuffledPosition) {
        this.shuffledPosition = shuffledPosition;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayQueueEntity{" +
                "id=" + id +
                ", audioId=" + audioId +
                ", position=" + position +
                ", shuffledPosition=" + shuffledPosition +
                '}';
    }
}
