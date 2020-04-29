package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.annotation.NonNull;
import androidx.room.Embedded;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class PlayQueueItemDto {

    private final long itemId;

    @Embedded
    private final Composition composition;

    public PlayQueueItemDto(long itemId,
                            Composition composition) {
        this.itemId = itemId;
        this.composition = composition;
    }

    public long getItemId() {
        return itemId;
    }

    public Composition getComposition() {
        return composition;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayQueueCompositionDto{" +
                "itemId=" + itemId +
                ", composition=" + composition +
                '}';
    }
}
