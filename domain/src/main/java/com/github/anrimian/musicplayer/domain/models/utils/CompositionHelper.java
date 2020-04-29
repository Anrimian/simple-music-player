package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.ALBUM;
import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.CORRUPTED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_ADDED;
import static com.github.anrimian.musicplayer.domain.Payloads.DATE_MODIFIED;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.FILE_NAME;
import static com.github.anrimian.musicplayer.domain.Payloads.SIZE;
import static com.github.anrimian.musicplayer.domain.Payloads.TITLE;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class CompositionHelper {

    public static boolean areSourcesTheSame(@Nonnull Composition first, @Nonnull Composition second) {
        return Objects.equals(first.getAlbum(), second.getAlbum())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getDateAdded(), second.getDateAdded())
                && Objects.equals(first.getDateModified(), second.getDateModified())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFileName(), second.getFileName())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && first.getCorruptionType() == second.getCorruptionType();
    }

    public static boolean hasSourceChanges(@Nonnull Composition first, @Nonnull Composition second) {
        return first.getDuration() != second.getDuration() || first.getSize() != second.getSize();
    }

    public static List<Object> getChangePayload(Composition first, Composition second) {
        List<Object> payloads = new LinkedList<>();
        if (!Objects.equals(first.getAlbum(), second.getAlbum())) {
            payloads.add(ALBUM);
        }
        if (!Objects.equals(first.getArtist(), second.getArtist())) {
            payloads.add(ARTIST);
        }
        if (!Objects.equals(first.getDateAdded(), second.getDateAdded())) {
            payloads.add(DATE_ADDED);
        }
        if (!Objects.equals(first.getDateModified(), second.getDateModified())) {
            payloads.add(DATE_MODIFIED);
        }
        if (first.getDuration() != second.getDuration()) {
            payloads.add(DURATION);
        }
        if (!Objects.equals(first.getFileName(), second.getFileName())) {
            payloads.add(FILE_NAME);
        }
        if (first.getSize() != second.getSize()) {
            payloads.add(SIZE);
        }
        if (!Objects.equals(first.getTitle(), second.getTitle())) {
            payloads.add(TITLE);
        }
        if (first.getCorruptionType() != second.getCorruptionType()) {
            payloads.add(CORRUPTED);
        }
        return payloads;
    }

    public static String formatCompositionName(Composition composition) {
        String title = composition.getTitle();
        if (isEmpty(title)) {
            return formatFileName(composition.getFileName());
        }
        return title;
    }
}
