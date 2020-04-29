package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;

import java.util.List;

import javax.annotation.Nonnull;

public class PlayQueueItemHelper {

    public static boolean areSourcesTheSame(@Nonnull PlayQueueItem first,
                                            @Nonnull PlayQueueItem second) {
        return CompositionHelper.areSourcesTheSame(first.getComposition(), second.getComposition());
    }

    public static List<Object> getChangePayload(@Nonnull PlayQueueItem first,
                                                @Nonnull PlayQueueItem second) {
        return CompositionHelper.getChangePayload(first.getComposition(), second.getComposition());
    }

    public static boolean hasSourceChanges(@Nonnull PlayQueueItem first,
                                           @Nonnull PlayQueueItem second) {
        return CompositionHelper.hasSourceChanges(first.getComposition(), second.getComposition());
    }
}
