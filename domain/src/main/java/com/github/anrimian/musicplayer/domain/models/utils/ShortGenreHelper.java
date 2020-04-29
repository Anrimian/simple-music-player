package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class ShortGenreHelper {

    public static boolean areSourcesTheSame(@Nonnull ShortGenre first, @Nonnull ShortGenre second) {
        return Objects.equals(first.getName(), second.getName());
    }

    public static List<Object> getChangePayload(ShortGenre first, ShortGenre second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        return payloads;
    }

}
