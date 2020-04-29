package com.github.anrimian.musicplayer.ui.common.serialization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.Date;

public interface PlaylistSerializer {

    String ID = "id";
    String NAME = "name";
    String DATE_ADDED = "date_added";
    String DATE_MODIFIED = "date_modified";
    String COMPOSITIONS_COUNT = "compositions_count";
    String TOTAL_DURATION = "total_duration";

    static Bundle serialize(PlayList playList) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, playList.getId());
        bundle.putString(NAME, playList.getName());
        bundle.putLong(DATE_ADDED, playList.getDateAdded().getTime());
        bundle.putLong(DATE_MODIFIED, playList.getDateModified().getTime());
        bundle.putInt(COMPOSITIONS_COUNT, playList.getCompositionsCount());
        bundle.putLong(TOTAL_DURATION, playList.getTotalDuration());
        return bundle;
    }

    static PlayList deserialize(Bundle bundle) {
        return new PlayList(bundle.getLong(ID),
                bundle.getString(NAME),
                new Date(bundle.getLong(DATE_ADDED)),
                new Date(bundle.getLong(DATE_MODIFIED)),
                bundle.getInt(COMPOSITIONS_COUNT),
                bundle.getLong(TOTAL_DURATION));
    }
}
