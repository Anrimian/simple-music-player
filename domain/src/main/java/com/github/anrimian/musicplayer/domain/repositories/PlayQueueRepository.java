package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created on 16.04.2018.
 */
public interface PlayQueueRepository {

    Completable setPlayQueue(List<Composition> compositions);

    Completable setPlayQueue(List<Composition> compositions, int startPosition);

    Flowable<Integer> getCurrentItemPositionObservable();

    Observable<PlayQueueEvent> getCurrentQueueItemObservable();

    Flowable<List<PlayQueueItem>> getPlayQueueObservable();

    void setRandomPlayingEnabled(boolean enabled);

    Single<Integer> skipToNext();

    void skipToPrevious();

    void skipToItem(PlayQueueItem item);

    Completable removeQueueItem(PlayQueueItem item);

    Completable restoreDeletedItem();

    Completable swapItems(PlayQueueItem firstItem, PlayQueueItem secondItem);

    Completable addCompositionsToPlayNext(List<Composition> compositions);

    Completable addCompositionsToEnd(List<Composition> compositions);

    Single<Boolean> isCurrentCompositionAtEndOfQueue();

    void clearPlayQueue();
}
