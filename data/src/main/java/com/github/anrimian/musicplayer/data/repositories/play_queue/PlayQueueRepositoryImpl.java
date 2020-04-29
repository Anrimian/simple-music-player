package com.github.anrimian.musicplayer.data.repositories.play_queue;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayQueueRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;

import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.NO_ITEM;
import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;

public class PlayQueueRepositoryImpl implements PlayQueueRepository {

    private final PlayQueueDaoWrapper playQueueDao;
    private final SettingsRepository settingsPreferences;
    private final UiStateRepository uiStatePreferences;
    private final Scheduler scheduler;

    private final Flowable<List<PlayQueueItem>> playQueueObservable;
    private final Observable<PlayQueueEvent> currentItemObservable;

    private boolean firstItemEmitted;
    private boolean consumeDeletedItemEvent = false;

    public PlayQueueRepositoryImpl(PlayQueueDaoWrapper playQueueDao,
                                   SettingsRepository settingsPreferences,
                                   UiStateRepository uiStatePreferences,
                                   Scheduler scheduler) {
        this.playQueueDao = playQueueDao;
        this.settingsPreferences = settingsPreferences;
        this.uiStatePreferences = uiStatePreferences;
        this.scheduler = scheduler;

        playQueueObservable = settingsPreferences.getRandomPlayingObservable()
                .switchMap(playQueueDao::getPlayQueueObservable)
                .toFlowable(BackpressureStrategy.LATEST)
                .replay(1)
                .refCount();

        currentItemObservable = uiStatePreferences.getCurrentItemIdObservable()
                .switchMap(this::getPlayQueueEvent)
                .subscribeOn(scheduler)
                .replay(1)
                .refCount();

        subscribeOnPositionChange();
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions) {
        return setPlayQueue(compositions, NO_POSITION);
    }

    @Override
    public Completable setPlayQueue(List<Composition> compositions, int startPosition) {
        if (compositions.isEmpty()) {
            return Completable.complete();
        }
        return Completable.fromAction(() -> insertNewQueue(compositions, startPosition))
                .subscribeOn(scheduler);
    }

    @Override
    public Flowable<Integer> getCurrentItemPositionObservable() {
        return Observable.combineLatest(uiStatePreferences.getCurrentItemIdObservable(),
                settingsPreferences.getRandomPlayingObservable(),
                playQueueDao::getIndexPositionObservable)
                .switchMap(observable -> observable)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<PlayQueueEvent> getCurrentQueueItemObservable() {
        return currentItemObservable;
    }

    @Override
    public Flowable<List<PlayQueueItem>> getPlayQueueObservable() {
        return playQueueObservable;
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        Completable.fromAction(() -> {
            if (enabled) {
                long itemId = uiStatePreferences.getCurrentQueueItemId();
                playQueueDao.reshuffleQueue(itemId);
            }
            settingsPreferences.setRandomPlayingEnabled(enabled);
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public Single<Integer> skipToNext() {
        return Single.fromCallable(() -> {
            long currentItemId = uiStatePreferences.getCurrentQueueItemId();
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            long nextQueueItemId = playQueueDao.getNextQueueItemId(currentItemId, isShuffled);
            setCurrentItem(nextQueueItemId);

            return playQueueDao.getPosition(nextQueueItemId, isShuffled);
        }).subscribeOn(scheduler);
    }

    @Override
    public void skipToPrevious() {
        Completable.fromAction(() -> {
            long currentItemId = uiStatePreferences.getCurrentQueueItemId();
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            long nextQueueItemId = playQueueDao.getPreviousQueueItemId(currentItemId, isShuffled);
            setCurrentItem(nextQueueItemId);
        }).subscribeOn(scheduler)
                .subscribe();
    }

    @Override
    public void skipToItem(PlayQueueItem item) {
        setCurrentItem(item.getId());
    }

    @Override
    public Completable removeQueueItem(PlayQueueItem item) {
        return Completable.fromAction(() -> playQueueDao.deleteItem(item.getId()))
                .subscribeOn(scheduler);
    }

    @Override
    public Completable restoreDeletedItem() {
        return Completable.fromAction(() -> {
            Long restoredId = playQueueDao.restoreDeletedItem();
            if (uiStatePreferences.getCurrentQueueItemId() == NO_ITEM && restoredId != null) {
                setCurrentItem(restoredId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable swapItems(PlayQueueItem firstItem,
                                 PlayQueueItem secondItem) {
        return Completable.fromRunnable(() -> playQueueDao.swapItems(firstItem,
                secondItem,
                settingsPreferences.isRandomPlayingEnabled())
        ).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToPlayNext(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            long id = uiStatePreferences.getCurrentQueueItemId();
            long firstId = playQueueDao.addCompositionsToQueue(compositions, id);
            if (id == NO_ITEM) {
                setCurrentItem(firstId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Completable addCompositionsToEnd(List<Composition> compositions) {
        return Completable.fromRunnable(() -> {
            long id = uiStatePreferences.getCurrentQueueItemId();
            long firstId = playQueueDao.addCompositionsToEndQueue(compositions);
            if (id == NO_ITEM) {
                setCurrentItem(firstId);
            }
        }).subscribeOn(scheduler);
    }

    @Override
    public Single<Boolean> isCurrentCompositionAtEndOfQueue() {
        return Single.fromCallable(() -> {
            boolean isShuffled = settingsPreferences.isRandomPlayingEnabled();
            int currentPosition = playQueueDao.getPosition(
                    uiStatePreferences.getCurrentQueueItemId(),
                    isShuffled
            );
            return currentPosition == playQueueDao.getLastPosition(isShuffled);
        });
    }

    private void setCurrentItem(@Nullable Long itemId) {
        if (itemId == null) {
            itemId = NO_ITEM;
        }
        uiStatePreferences.setCurrentQueueItemId(itemId);
    }

    /**
     * Position saving for moving to next item after current item deleted
     */
    private void subscribeOnPositionChange() {
        Observable.combineLatest(uiStatePreferences.getCurrentItemIdObservable(),
                settingsPreferences.getRandomPlayingObservable(),
                playQueueDao::getPositionObservable)
                .switchMap(observable -> observable)
                .doOnNext(uiStatePreferences::setCurrentItemLastPosition)
                .subscribe();
    }

    private void insertNewQueue(List<Composition> compositions, int startPosition) {
        consumeDeletedItemEvent = true;
        long itemId = playQueueDao.insertNewPlayQueue(compositions,
                settingsPreferences.isRandomPlayingEnabled(),
                startPosition);
        setCurrentItem(itemId);
        consumeDeletedItemEvent = false;
    }

    private Observable<PlayQueueEvent> getPlayQueueEvent(long id) {
        if (id == NO_ITEM) {
            return Observable.just(new PlayQueueEvent(null));
        }

        return playQueueDao.getItemObservable(id)
                .flatMap(this::checkForExisting)
                .map(this::mapToQueueEvent);
    }

    private Observable<PlayQueueItem> checkForExisting(Optional<PlayQueueItem> itemOpt) {
        return Observable.create(emitter -> {
            PlayQueueItem item = itemOpt.getValue();
            if (item == null) {
                if (consumeDeletedItemEvent) {
                    return;
                }
                //handle deleted item
                boolean isRandom = settingsPreferences.isRandomPlayingEnabled();
                int lastPosition = uiStatePreferences.getCurrentItemLastPosition();
                Long nextItemId = playQueueDao.getItemAtPosition(lastPosition, isRandom);
                if (nextItemId == null) {
                    nextItemId = playQueueDao.getItemAtPosition(0, isRandom);
                }
                setCurrentItem(nextItemId);
                return;
            }
            emitter.onNext(item);
        });
    }

    private PlayQueueEvent mapToQueueEvent(PlayQueueItem item) {
        long trackPosition = 0;
        if (!firstItemEmitted) {
            firstItemEmitted = true;
            trackPosition = uiStatePreferences.getTrackPosition();
        }
        return new PlayQueueEvent(item, trackPosition);
    }
}
