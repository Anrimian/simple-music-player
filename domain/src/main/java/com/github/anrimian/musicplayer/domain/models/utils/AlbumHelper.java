package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class AlbumHelper {

    public static boolean areSourcesTheSame(@Nonnull Album first, @Nonnull Album second) {
        return Objects.equals(first.getName(), second.getName())
                && Objects.equals(first.getArtist(), second.getArtist())
                && first.getCompositionsCount() == second.getCompositionsCount();
    }

    public static List<Object> getChangePayload(Album first, Album second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        if (!Objects.equals(first.getArtist(), second.getArtist())) {
            payloads.add(ARTIST);
        }
        if (first.getCompositionsCount() != second.getCompositionsCount()) {
            payloads.add(COMPOSITIONS_COUNT);
        }
        return payloads;
    }
}
