package com.github.anrimian.musicplayer.ui.common.serialization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;

public interface ArtistSerializer {

    String ID = "id";
    String NAME = "name";
    String COMPOSITION_COUNT = "composition_count";
    String ALBUMS_COUNT = "albums_count";

    static Bundle serialize(Artist artist) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, artist.getId());
        bundle.putString(NAME, artist.getName());
        bundle.putInt(COMPOSITION_COUNT, artist.getCompositionsCount());
        bundle.putInt(ALBUMS_COUNT, artist.getAlbumsCount());
        return bundle;
    }

    static Artist deserialize(Bundle bundle) {
        return new Artist(bundle.getLong(ID),
                bundle.getString(NAME),
                bundle.getInt(COMPOSITION_COUNT),
                bundle.getInt(ALBUMS_COUNT));
    }
}
