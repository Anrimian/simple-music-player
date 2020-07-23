package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueItemDto;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.NO_ITEM;
import static com.github.anrimian.musicplayer.domain.Constants.NO_POSITION;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 02.07.2018.
 */
public class PlayQueueDaoWrapper {

    private final AppDatabase appDatabase;
    private final PlayQueueDao playQueueDao;

    @Nullable
    private PlayQueueEntity deletedItem;

    public PlayQueueDaoWrapper(AppDatabase appDatabase, PlayQueueDao playQueueDao) {
        this.appDatabase = appDatabase;
        this.playQueueDao = playQueueDao;
    }

    public Observable<List<PlayQueueItem>> getPlayQueueObservable(boolean isRandom) {
        return isRandom? getPlayQueueInShuffledOrderObservable(): getPlayQueueInNormalOrderObservable();
    }

    public void reshuffleQueue(long currentItemId) {
        appDatabase.runInTransaction(() -> {
            List<PlayQueueEntity> list = playQueueDao.getPlayQueue();

            Collections.shuffle(list);

            long firstItemId = list.get(0).getId();
            int currentItemPosition = -1;
            for (int i = 0; i < list.size(); i++) {
                PlayQueueEntity entity = list.get(i);

                if (entity.getId() == currentItemId) {
                    currentItemPosition = i;
                }
                entity.setShuffledPosition(i);
            }
            if (currentItemPosition != -1 && firstItemId != currentItemId) {
                list.get(currentItemPosition).setShuffledPosition(0);
                list.get(0).setShuffledPosition(currentItemPosition);
            }

            playQueueDao.deletePlayQueue();
            playQueueDao.insertItems(list);
        });
    }

    public long insertNewPlayQueue(List<Composition> compositions,
                                   boolean randomPlayingEnabled,
                                   int startPosition) {
        return appDatabase.runInTransaction(() -> {
            List<Composition> shuffledList = new ArrayList<>(compositions);
            long randomSeed = System.nanoTime();
            Collections.shuffle(shuffledList, new Random(randomSeed));

            List<Integer> shuffledPositionList = new ArrayList<>(compositions.size());
            for (int i = 0; i < compositions.size(); i++) {
                shuffledPositionList.add(i);
            }
            Collections.shuffle(shuffledPositionList, new Random(randomSeed));

            List<PlayQueueEntity> entities = new ArrayList<>(compositions.size());
            int shuffledStartPosition = 0;
            for (int i = 0; i < compositions.size(); i++) {
                Composition composition = compositions.get(i);
                PlayQueueEntity playQueueEntity = new PlayQueueEntity();
                playQueueEntity.setAudioId(composition.getId());
                playQueueEntity.setPosition(i);
                int shuffledPosition =  shuffledPositionList.get(i);
                playQueueEntity.setShuffledPosition(shuffledPosition);

                if (startPosition != NO_POSITION && i == startPosition) {
                    shuffledStartPosition = shuffledPosition;
                }

                entities.add(playQueueEntity);
            }

            playQueueDao.deletePlayQueue();
            playQueueDao.insertItems(entities);

            if (randomPlayingEnabled) {
                return playQueueDao.getItemIdAtShuffledPosition(shuffledStartPosition);
            } else {
                return playQueueDao.getItemIdAtPosition(startPosition == NO_POSITION? 0: startPosition);
            }
        });
    }

    public Observable<Optional<PlayQueueItem>> getItemObservable(long id) {
        return playQueueDao.getItemObservable(id)
                .map(dto -> {
                    PlayQueueItem item = null;
                    if (dto.length > 0) {
                        item = toQueueItem(dto[0]);
                    }
                    return new Optional<>(item);
                });
    }

    public void deleteItem(long itemId) {
        deletedItem = playQueueDao.getItem(itemId);
        playQueueDao.deleteItem(itemId);
    }

    @Nullable
    public Long restoreDeletedItem() {
        if (deletedItem != null) {
           return playQueueDao.insertItem(deletedItem);
        }
        return null;
    }

    public void swapItems(PlayQueueItem firstItem, PlayQueueItem secondItem, boolean shuffleMode) {
        appDatabase.runInTransaction(() -> {
            long firstId = firstItem.getId();
            long secondId = secondItem.getId();
            if (shuffleMode) {
                int firstPosition = playQueueDao.getShuffledPosition(firstId);
                int secondPosition = playQueueDao.getShuffledPosition(secondId);

                playQueueDao.updateShuffledPosition(secondId, Integer.MIN_VALUE);
                playQueueDao.updateShuffledPosition(firstId, secondPosition);
                playQueueDao.updateShuffledPosition(secondId, firstPosition);
            } else {
                int firstPosition = playQueueDao.getPosition(firstId);
                int secondPosition = playQueueDao.getPosition(secondId);

                playQueueDao.updateItemPosition(secondId, Integer.MIN_VALUE);
                playQueueDao.updateItemPosition(firstId, secondPosition);
                playQueueDao.updateItemPosition(secondId, firstPosition);
            }
        });
    }

    public long addCompositionsToEndQueue(List<Composition> compositions) {
        return appDatabase.runInTransaction(() -> {
            int positionToInsert = playQueueDao.getLastPosition() + 1;
            int shuffledPositionToInsert = playQueueDao.getLastShuffledPosition() + 1;
            List<PlayQueueEntity> entities = toEntityList(compositions,
                    positionToInsert,
                    shuffledPositionToInsert);
            long[] ids = playQueueDao.insertItems(entities);
            return ids[0];
        });
    }

    public long addCompositionsToQueue(List<Composition> compositions, long currentItemId) {
        return appDatabase.runInTransaction(() -> {
            int positionToInsert = 0;
            int shuffledPositionToInsert = 0;
            if (currentItemId != NO_ITEM) {
                int currentPosition = playQueueDao.getPosition(currentItemId);
                int currentShuffledPosition = playQueueDao.getShuffledPosition(currentItemId);

                int increaseBy = compositions.size();
                int lastPosition = playQueueDao.getLastPosition();
                for (int pos = lastPosition; pos > currentPosition; pos--) {
                    playQueueDao.increasePosition(increaseBy, pos);
                }
                int lastShuffledPosition = playQueueDao.getLastShuffledPosition();
                for (int pos = lastShuffledPosition; pos > currentShuffledPosition; pos--) {
                    playQueueDao.increaseShuffledPosition(increaseBy, pos);
                }

                positionToInsert = currentPosition + 1;
                shuffledPositionToInsert = currentShuffledPosition + 1;
            }

            List<PlayQueueEntity> entities = toEntityList(compositions,
                    positionToInsert,
                    shuffledPositionToInsert);
            long[] ids = playQueueDao.insertItems(entities);
            return ids[0];
        });
    }

    public int getPosition(long id, boolean isShuffle) {
        if (isShuffle) {
            return playQueueDao.getShuffledPosition(id);
        } else {
            return playQueueDao.getPosition(id);
        }
    }

    public int getLastPosition(boolean isShuffled) {
        if (isShuffled) {
            return playQueueDao.getLastShuffledPosition();
        } else {
            return playQueueDao.getLastPosition();
        }
    }

    public Observable<Integer> getPositionObservable(long id, boolean isShuffle) {
        Observable<Integer> observable;
        if (isShuffle) {
            observable = playQueueDao.getShuffledPositionObservable(id);
        } else {
            observable = playQueueDao.getPositionObservable(id);
        }
        return observable.distinctUntilChanged();
    }

    public Observable<Integer> getIndexPositionObservable(long id, boolean isShuffle) {
        Observable<Integer> observable;
        if (isShuffle) {
            observable = playQueueDao.getShuffledIndexPositionObservable(id);
        } else {
            observable = playQueueDao.getIndexPositionObservable(id);
        }
        return observable.filter(pos -> pos >= 0)
                .distinctUntilChanged();
    }

    public long getNextQueueItemId(long currentItemId, boolean isShuffled) {
        if (isShuffled) {
            Long id = playQueueDao.getNextShuffledQueueItemId(currentItemId);
            if (id == null) {
                return playQueueDao.getFirstShuffledItem();
            }
            return id;
        } else {
            Long id = playQueueDao.getNextQueueItemId(currentItemId);
            if (id == null) {
                return playQueueDao.getFirstItem();
            }
            return id;
        }
    }

    public long getPreviousQueueItemId(long currentItemId, boolean isShuffled) {
        if (isShuffled) {
            Long id = playQueueDao.getPreviousShuffledQueueItemId(currentItemId);
            if (id == null) {
                return playQueueDao.getLastShuffledItem();
            }
            return id;
        } else {
            Long id = playQueueDao.getPreviousQueueItemId(currentItemId);
            if (id == null) {
                return playQueueDao.getLastItem();
            }
            return id;
        }
    }

    public Long getItemAtPosition(int position, boolean isShuffled) {
        if (isShuffled) {
            return playQueueDao.getItemIdAtShuffledPosition(position);
        } else {
            return playQueueDao.getItemIdAtPosition(position);
        }
    }

    public void deletePlayQueue() {
        playQueueDao.deletePlayQueue();
    }

    private Observable<List<PlayQueueItem>> getPlayQueueInNormalOrderObservable() {
        return playQueueDao.getPlayQueueInNormalOrderObservable()
                .map(list -> mapList(list, this::toQueueItem));
    }

    private Observable<List<PlayQueueItem>> getPlayQueueInShuffledOrderObservable() {
        return playQueueDao.getPlayQueueInShuffledOrderObservable()
                .map(list -> mapList(list, this::toQueueItem));
    }

    private List<PlayQueueEntity> toEntityList(List<Composition> compositions,
                                               int position,
                                               int shuffledPosition) {
        List<PlayQueueEntity> entityList = new ArrayList<>(compositions.size());

        for (Composition composition: compositions) {
            PlayQueueEntity playQueueEntity = new PlayQueueEntity();
            playQueueEntity.setAudioId(composition.getId());
            playQueueEntity.setPosition(position++);
            playQueueEntity.setShuffledPosition(shuffledPosition++);

            entityList.add(playQueueEntity);
        }
        return entityList;
    }

    private PlayQueueItem toQueueItem(PlayQueueItemDto dto) {
        return new PlayQueueItem(dto.getItemId(), dto.getComposition());
    }
}
