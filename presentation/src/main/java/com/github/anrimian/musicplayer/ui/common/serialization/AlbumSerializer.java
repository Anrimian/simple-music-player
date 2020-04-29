package com.github.anrimian.musicplayer.ui.common.serialization;

import android.os.Bundle;

import com.github.anrimian.musicplayer.domain.models.albums.Album;

public interface AlbumSerializer {

    String ID = "id";
    String NAME = "name";
    String ARTIST = "artist";
    String COMPOSITION_COUNT = "composition_count";

    static Bundle serialize(Album album) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID, album.getId());
        bundle.putString(NAME, album.getName());
        bundle.putString(ARTIST, album.getArtist());
        bundle.putInt(COMPOSITION_COUNT, album.getCompositionsCount());
        return bundle;
    }

    static Album deserialize(Bundle bundle) {
        return new Album(bundle.getLong(ID),
                bundle.getString(NAME),
                bundle.getString(ARTIST),
                bundle.getInt(COMPOSITION_COUNT)
        );
    }
}
