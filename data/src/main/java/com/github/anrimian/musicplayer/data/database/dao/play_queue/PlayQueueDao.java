package com.github.anrimian.musicplayer.data.database.dao.play_queue;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueItemDto;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface PlayQueueDao {

    @Query("SELECT * FROM play_queue ORDER BY position")
    List<PlayQueueEntity> getPlayQueue();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "compositions.title AS title, " +
            "compositions.fileName AS fileName, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY position")
    Observable<List<PlayQueueItemDto>> getPlayQueueInNormalOrderObservable();

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "compositions.storageId AS storageId, " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "compositions.title AS title, " +
            "compositions.fileName AS fileName, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "ORDER BY shuffledPosition")
    Observable<List<PlayQueueItemDto>> getPlayQueueInShuffledOrderObservable();

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE position >= :position " +
            "LIMIT 1")
    Long getItemIdAtPosition(int position);

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE shuffledPosition >= :position " +
            "LIMIT 1")
    Long getItemIdAtShuffledPosition(int position);

    @Query("SELECT " +
            "play_queue.id AS itemId," +
            "compositions.id AS id, " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "compositions.storageId AS storageId, " +
            "compositions.title AS title, " +
            "compositions.fileName AS fileName, " +
            "compositions.duration AS duration, " +
            "compositions.size AS size, " +
            "compositions.dateAdded AS dateAdded, " +
            "compositions.dateModified AS dateModified, " +
            "compositions.corruptionType AS corruptionType " +
            "FROM play_queue INNER JOIN compositions ON play_queue.audioId = compositions.id " +
            "WHERE itemId = :id " +
            "LIMIT 1")
    Observable<PlayQueueItemDto[]> getItemObservable(long id);

    @Insert
    long[] insertItems(List<PlayQueueEntity> playQueueEntityList);

    @Insert
    long insertItem(PlayQueueEntity entity);

    @Query("DELETE FROM play_queue")
    void deletePlayQueue();

    @Query("DELETE FROM play_queue WHERE id = :id")
    void deleteItem(long id);

    @Query("DELETE FROM play_queue WHERE audioId in (:audioIds)")
    void delete(List<Long> audioIds);

    @Query("SELECT position FROM play_queue WHERE id = :id")
    int getPosition(long id);

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    int getShuffledPosition(long id);

    @Query("SELECT * FROM play_queue WHERE id = :id")
    PlayQueueEntity getItem(long id);

    @Query("SELECT position FROM play_queue WHERE id = :id")
    Observable<Integer> getPositionObservable(long id);

    @Query("SELECT shuffledPosition FROM play_queue WHERE id = :id")
    Observable<Integer> getShuffledPositionObservable(long id);

    @Query("WITH item AS (SELECT position FROM play_queue WHERE id = :id) " +
            "SELECT CASE WHEN item.position IS NULL " +
            "  THEN -1 " +
            "  ELSE (SELECT count() FROM play_queue WHERE position < item.position)" +
            "END " +
            "AS result " +
            "FROM play_queue, item " +
            "LIMIT 1")
    Observable<Integer> getIndexPositionObservable(long id);

    @Query("WITH item AS (SELECT shuffledPosition FROM play_queue WHERE id = :id) " +
            "SELECT CASE WHEN item.shuffledPosition IS NULL " +
            "  THEN -1 " +
            "  ELSE (SELECT count() FROM play_queue WHERE shuffledPosition < item.shuffledPosition)" +
            "END " +
            "AS result " +
            "FROM play_queue, item " +
            "LIMIT 1")
    Observable<Integer> getShuffledIndexPositionObservable(long id);

    @Query("UPDATE play_queue SET shuffledPosition = :shuffledPosition WHERE id = :id")
    void updateShuffledPosition(long id, int shuffledPosition);

    @Query("UPDATE play_queue SET position = :position WHERE id = :itemId")
    void updateItemPosition(long itemId, int position);

    @Query("UPDATE play_queue " +
            "SET position = position + :increaseBy " +
            "WHERE position = :position")
    void increasePosition(int increaseBy, int position);

    @Query("UPDATE play_queue " +
            "SET shuffledPosition = shuffledPosition + :increaseBy " +
            "WHERE shuffledPosition = :position")
    void increaseShuffledPosition(int increaseBy, int position);

    @Query("SELECT MAX(position) FROM play_queue")
    int getLastPosition();

    @Query("SELECT MAX(shuffledPosition) FROM play_queue")
    int getLastShuffledPosition();

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE position = (SELECT MAX(position) FROM play_queue)")
    long getLastItem();

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE shuffledPosition = (SELECT MAX(shuffledPosition) FROM play_queue)")
    long getLastShuffledItem();

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE position = (SELECT MIN(position) FROM play_queue)")
    long getFirstItem();

    @Query("SELECT id " +
            "FROM play_queue " +
            "WHERE shuffledPosition = (SELECT MIN(shuffledPosition) FROM play_queue)")
    long getFirstShuffledItem();

    @Update
    void update(List<PlayQueueEntity> list);

    @Query("SELECT id " +
            "FROM play_queue "+
            "WHERE position = " +
            "   (SELECT MIN(position) " +
            "   FROM play_queue " +
            "   WHERE position > " +
            "       (SELECT position FROM play_queue WHERE id = :currentItemId))")
    Long getNextQueueItemId(long currentItemId);

    @Query("SELECT id " +
            "FROM play_queue "+
            "WHERE shuffledPosition = " +
            "   (SELECT MIN(shuffledPosition) " +
            "   FROM play_queue " +
            "   WHERE shuffledPosition > " +
            "       (SELECT shuffledPosition FROM play_queue WHERE id = :currentItemId))")
    Long getNextShuffledQueueItemId(long currentItemId);

    @Query("SELECT id " +
            "FROM play_queue "+
            "WHERE position = " +
            "   (SELECT MAX(position) " +
            "   FROM play_queue " +
            "   WHERE position < " +
            "       (SELECT position FROM play_queue WHERE id = :currentItemId)" +
            "       AND (SELECT corruptionType FROM compositions WHERE id = audioId) IS NULL)")
    Long getPreviousQueueItemId(long currentItemId);

    @Query("SELECT id " +
            "FROM play_queue "+
            "WHERE shuffledPosition = " +
            "   (SELECT MAX(shuffledPosition) " +
            "   FROM play_queue " +
            "   WHERE shuffledPosition < " +
            "       (SELECT shuffledPosition FROM play_queue WHERE id = :currentItemId)" +
            "       AND (SELECT corruptionType FROM compositions WHERE id = audioId) IS NULL)")
    Long getPreviousShuffledQueueItemId(long currentItemId);
}
