package com.github.anrimian.musicplayer.domain.interactors;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 16.04.2018.
 */
public class TestBusinessDataProvider {

    public static List<Composition> getFakeCompositions() {
        List<Composition> compositions = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
            compositions.add(composition);
        }
        return compositions;
    }

    private static List<PlayListItem> getFakePlayListItems() {
        List<PlayListItem> items = new ArrayList<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
            PlayListItem item = new PlayListItem(i, composition);
            items.add(item);
        }
        return items;
    }

    private static List<PlayQueueItem> getFakeItems() {
        List<PlayQueueItem> items = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
            PlayQueueItem item = new PlayQueueItem(i, composition);
            items.add(item);
        }
        return items;
    }

    public static List<PlayQueueItem> getReversedFakeItems() {
        List<PlayQueueItem> items = getFakeItems();
        Collections.reverse(items);
        return items;
    }

    public static Composition fakeComposition(long id) {
        return new Composition(null,
                null,
                "fileName",
                null,
                0,
                0,
                id,
                id,
                new Date(0),
                new Date(0),
                null);
    }

    public static PlayListItem fakePlayListItem(int index) {
        return getFakePlayListItems().get(index);
    }


    public static PlayQueueItem fakeItem(int index) {
        return getFakeItems().get(index);
    }

    public static Map<Long, Composition> getFakeCompositionsMap() {
        Map<Long, Composition> compositions = new HashMap<>();
        for (long i = 0; i < 100000; i++) {
            Composition composition = fakeComposition(i, "music-" + i);
            compositions.put(i, composition);
        }
        return compositions;
    }

    public static PlayQueueEvent currentItem(int pos) {
        return new PlayQueueEvent(new PlayQueueItem(pos, fakeComposition(pos)), 0L);
    }

    public static PlayQueueEvent currentItem(int itemId, int compositionId) {
        return new PlayQueueEvent(new PlayQueueItem(itemId, fakeComposition(compositionId)), 0L);
    }

    public static Composition fakeComposition(long id, String fileName) {
        return new Composition(null,
                null,
                fileName,
                null,
                0,
                0,
                id,
                id,
                new Date(0),
                new Date(0),
                null);
    }

}
