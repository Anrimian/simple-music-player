package com.github.anrimian.musicplayer.data.database.mappers;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

public class CompositionCorruptionDetector {

    public static CorruptionType getCorruptionType(StorageFullComposition composition) {
        CorruptionType corruptionType = null;
        if (composition.getDuration() == 0) {
            corruptionType = CorruptionType.UNKNOWN;
        }
        return corruptionType;
    }
}
