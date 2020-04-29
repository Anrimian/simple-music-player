package com.github.anrimian.musicplayer.data.database.dao.play_list;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

@Dao
public interface PlayListDao {

    @Insert
    long insertPlayListEntity(PlayListEntity entity);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlayListEntities(List<PlayListEntity> entities);

    @Query("DELETE FROM play_lists WHERE id = :id")
    void deletePlayList(long id);

    @Query("UPDATE play_lists SET name = :name WHERE id = :id")
    void updatePlayListName(long id, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE id = :id")
    void updatePlayListModifyTime(long id, Date modifyTime);

    @Query("UPDATE play_lists SET name = :name WHERE storageId = :storageId")
    void updatePlayListNameByStorageId(long storageId, String name);

    @Query("UPDATE play_lists SET dateModified = :modifyTime WHERE storageId = :storageId")
    void updatePlayListModifyTimeByStorageId(long storageId, Date modifyTime);

    //can we cache duplicates?
    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM play_lists_entries WHERE playListId = play_lists.id)) as totalDuration " +
            "FROM play_lists " +
            "ORDER BY dateModified DESC")
    Observable<List<PlayList>> getPlayListsObservable();

    @Query("SELECT " +
            "play_lists.storageId as storageId, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified " +
            "FROM play_lists " +
            "WHERE storageId IS NOT NULL")
    List<StoragePlayList> getAllAsStoragePlayLists();

    @Query("SELECT " +
            "play_lists.id as dbId, " +
            "play_lists.storageId as storageId " +
            "FROM play_lists WHERE play_lists.storageId IS NOT NULL")
    List<IdPair> getPlayListsIds();

    @Query("SELECT " +
            "play_lists.id as id, " +
            "play_lists.name as name, " +
            "play_lists.dateAdded as dateAdded, " +
            "play_lists.dateModified as dateModified, " +
            "(SELECT count() FROM play_lists_entries WHERE playListId = play_lists.id) as compositionsCount, " +
            "(SELECT sum(duration) FROM compositions WHERE compositions.id IN (SELECT audioId FROM play_lists_entries WHERE playListId = play_lists.id)) as totalDuration " +
            "FROM play_lists " +
            "WHERE play_lists.id = :id ")
    Observable<List<PlayList>> getPlayListObservable(long id);

    @Query("SELECT " +
            "play_lists_entries.itemId AS itemId," +
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
            "FROM play_lists_entries " +
            "INNER JOIN compositions ON play_lists_entries.audioId = compositions.id " +
            "WHERE play_lists_entries.playListId = :playListId " +
            "ORDER BY orderPosition")
    Observable<List<PlayListEntryDto>> getPlayListItemsObservable(long playListId);

    @Query("SELECT " +
            "play_lists_entries.storageItemId as itemId, " +
            "(SELECT storageId FROM compositions WHERE id = audioId) as audioId " +
            "FROM play_lists_entries " +
            "WHERE play_lists_entries.playListId = :playListId " +
            "ORDER BY orderPosition")
    List<StoragePlayListItem> getPlayListItemsAsStorageItems(long playListId);

    @Query("DELETE FROM play_lists_entries WHERE itemId = :id")
    void deletePlayListEntry(long id);

    @Insert
    void insertPlayListEntries(List<PlayListEntryEntity> entities);

    @Query("SELECT MAX(orderPosition) FROM play_lists_entries WHERE playListId = :playListId")
    int selectMaxOrder(long playListId);

    @Query("SELECT orderPosition FROM play_lists_entries WHERE itemId = :id")
    int selectPositionById(long id);

    @Query("UPDATE play_lists_entries SET orderPosition = " +
            "  case " +
            "    when orderPosition < :fromPos then orderPosition + 1" +
            "    when orderPosition > :fromPos then orderPosition - 1" +
            "    else :toPos" +
            "  end " +
            "WHERE (orderPosition between min(:fromPos, :toPos) and max(:fromPos,:toPos)) " +
            "AND playListId = :playListId")
    void moveItems(long playListId, int fromPos, int toPos);

    @Query("UPDATE play_lists_entries " +
            "SET orderPosition = orderPosition + :increaseBy " +
            "WHERE orderPosition >= :position AND playListId = :playListId")
    void increasePositionsByCountAfter(int increaseBy, int position, long playListId);

    @Query("UPDATE play_lists_entries " +
            "SET orderPosition = orderPosition - 1 " +
            "WHERE orderPosition > :position AND playListId = :playListId")
    void decreasePositionsAfter(int position, long playListId);

    @Nullable
    @Query("SELECT storageId FROM play_lists WHERE id = :id")
    Long selectStorageId(long id);

    @Nullable
    @Query("SELECT storageItemId FROM play_lists_entries WHERE itemId = :itemId")
    Long selectStorageItemId(long itemId);

    @Query("UPDATE play_lists SET storageId = :storageId WHERE id = :id")//update entries?
    void updateStorageId(long id, Long storageId);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE name = :name LIMIT 1)")
    boolean isPlayListWithNameExists(String name);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE id = :playListId LIMIT 1)")
    boolean isPlayListExists(long playListId);

    @Query("SELECT exists(SELECT 1 FROM play_lists WHERE storageId = :storageId LIMIT 1)")
    boolean isPlayListExistsByStorageId(long storageId);
}
