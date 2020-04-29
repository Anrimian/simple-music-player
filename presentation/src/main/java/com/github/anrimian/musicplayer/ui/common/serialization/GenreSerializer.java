package com.github.anrimian.musicplayer.ui.common.serialization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;

public interface GenreSerializer {

    String ID = "id";
    String NAME = "name";
    String COMPOSITION_COUNT = "composition_count";
    String TOTAL_DURATION = "total_duration";

    static Bundle serialize(Genre genre) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, genre.getId());
        bundle.putString(NAME, genre.getName());
        bundle.putInt(COMPOSITION_COUNT, genre.getCompositionsCount());
        bundle.putLong(TOTAL_DURATION, genre.getTotalDuration());
        return bundle;
    }

    static Bundle serialize(ShortGenre genre) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, genre.getId());
        bundle.putString(NAME, genre.getName());
        return bundle;
    }

    static Genre deserialize(Bundle bundle) {
        return new Genre(bundle.getLong(ID),
                bundle.getString(NAME),
                bundle.getInt(COMPOSITION_COUNT),
                bundle.getLong(TOTAL_DURATION));
    }

    static ShortGenre deserializeShort(Bundle bundle) {
        return new ShortGenre(bundle.getLong(ID), bundle.getString(NAME));
    }
}
