package com.github.anrimian.musicplayer.domain.models.utils;

import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.Payloads.FILES_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;

public class FolderHelper {

    public static boolean areSourcesTheSame(FileSource oldSource, FileSource newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource) {
                return !FolderHelper.hasChanges(((FolderFileSource) oldSource),
                        ((FolderFileSource) newSource));
            }
            if (oldSource instanceof CompositionFileSource) {
                return CompositionHelper.areSourcesTheSame(((CompositionFileSource) oldSource).getComposition(),
                        ((CompositionFileSource) newSource).getComposition());
            }
        }
        return false;
    }

    public static List<Object> getChangePayload(FileSource oldSource, FileSource newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource) {
                return FolderHelper.getChangePayload(((FolderFileSource) oldSource),
                        ((FolderFileSource) newSource));
            }
            if (oldSource instanceof CompositionFileSource) {
                return CompositionHelper.getChangePayload(
                        ((CompositionFileSource) oldSource).getComposition(),
                        ((CompositionFileSource) newSource).getComposition());
            }
        }
        return null;
    }

    public static boolean hasChanges(@Nonnull FolderFileSource first, @Nonnull FolderFileSource second) {
        return /*!Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())
                || */first.getFilesCount() != second.getFilesCount()
                || !Objects.equals(first.getName(), second.getName());
    }

    private static List<Object> getChangePayload(FolderFileSource first, FolderFileSource second) {
        List<Object> payloads = new LinkedList<>();
//        if (!Objects.equals(first.getEarliestCreateDate(), second.getEarliestCreateDate())) {
//            payloads.add(CREATE_DATE);
//        }
        if (first.getFilesCount() != second.getFilesCount()) {
            payloads.add(FILES_COUNT);
        }
        if (!Objects.equals(first.getName(), second.getName())) {
            payloads.add(NAME);
        }
        return payloads;
    }
}
