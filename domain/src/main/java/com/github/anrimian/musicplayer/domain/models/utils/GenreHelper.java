package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class GenreHelper {

    public static boolean areSourcesTheSame(@Nonnull Genre first, @Nonnull Genre second) {
        return Objects.equals(first.getName(), second.getName())
                && first.getCompositionsCount() == second.getCompositionsCount()
                && first.getTotalDuration() == second.getTotalDuration();
    }

    public static List<Object> getChangePayload(Genre first, Genre second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        if (first.getCompositionsCount() != second.getCompositionsCount()) {
            payloads.add(COMPOSITIONS_COUNT);
        }
        if (first.getTotalDuration() != second.getTotalDuration()) {
            payloads.add(DURATION);
        }
        return payloads;
    }
}
