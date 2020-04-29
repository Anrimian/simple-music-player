package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class ArtistHelper {

    public static boolean areSourcesTheSame(@Nonnull Artist first, @Nonnull Artist second) {
        return Objects.equals(first.getName(), second.getName())
                && first.getCompositionsCount() == second.getCompositionsCount();
    }

    public static List<Object> getChangePayload(Artist first, Artist second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        if (first.getCompositionsCount() != second.getCompositionsCount()) {
            payloads.add(COMPOSITIONS_COUNT);
        }
        return payloads;
    }
}
