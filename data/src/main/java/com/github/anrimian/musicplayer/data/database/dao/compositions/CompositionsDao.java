package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

@Dao
public interface CompositionsDao {

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "fileName as fileName, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "corruptionType as corruptionType " +
            "FROM compositions " +
            "WHERE id = :id " +
            "LIMIT 1")
    Observable<List<FullComposition>> getCompositionObservable(long id);

    @RawQuery(observedEntities = { CompositionEntity.class, ArtistEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getAllObservable(SupportSQLiteQuery query);

    @RawQuery(observedEntities = { CompositionEntity.class, ArtistEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getAllInFolderObservable(SupportSQLiteQuery query);

    @RawQuery
    List<Composition> executeQuery(SimpleSQLiteQuery sqlQuery);

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "compositions.fileName as fileName, " +
            "compositions.filePath as filePath, " +
            "compositions.duration as duration, " +
            "compositions.size as size, " +
            "compositions.id as id, " +
            "compositions.storageId as storageId, " +
            "compositions.folderId as folderId, " +
            "compositions.dateAdded as dateAdded, " +
            "compositions.dateModified as dateModified " +
            "FROM compositions WHERE storageId NOTNULL")
    List<StorageComposition> selectAllAsStorageCompositions();

    @Insert
    long insert(CompositionEntity entity);

    @Insert
    void insert(List<CompositionEntity> entities);

    @Query("UPDATE compositions SET " +
            "title = :title, " +
            "fileName = :fileName, " +
            "filePath = :filePath, " +
            "duration = :duration, " +
            "size = :size, " +
            "dateAdded = :dateAdded, " +
            "dateModified = :dateModified " +
            "WHERE storageId = :storageId")
    void update(String title,
                String fileName,
                String filePath,
                long duration,
                long size,
                Date dateAdded,
                Date dateModified,
                long storageId);

    @Query("DELETE FROM compositions WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM compositions WHERE id in (:ids)")
    void delete(List<Long> ids);

    @Query("UPDATE compositions SET filePath = :filePath WHERE id = :id")
    void updateFilePath(long id, String filePath);

    @Query("UPDATE compositions SET artistId = :artistId WHERE id = :id")
    void updateArtist(long id, Long artistId);

    @Query("UPDATE compositions SET albumId = :albumId WHERE id = :id")
    void updateAlbum(long id, Long albumId);

    @Query("UPDATE compositions SET title = :title WHERE id = :id")
    void updateTitle(long id, String title);

    @Query("UPDATE compositions SET fileName = :fileName WHERE id = :id")
    void updateCompositionFileName(long id, String fileName);

    @Query("UPDATE compositions SET folderId = :folderId WHERE id = :id")
    void updateFolderId(long id, Long folderId);

    @Query("SELECT id FROM compositions WHERE storageId = :storageId")
    long selectIdByStorageId(long storageId);

    @Query("SELECT storageId FROM compositions WHERE id = :id")
    Long getStorageId(long id);

    @Query("UPDATE compositions SET corruptionType = :corruptionType WHERE id = :id")
    void setCorruptionType(CorruptionType corruptionType, long id);

    @Query("SELECT albumId FROM compositions WHERE id = :compositionId")
    Long getAlbumId(long compositionId);

    @Query("UPDATE compositions SET albumId = :newAlbumId WHERE id = :compositionId")
    void setAlbumId(long compositionId, long newAlbumId);

    @Query("SELECT artistId FROM compositions WHERE id = :id")
    Long getArtistId(long id);

    @Query("UPDATE compositions SET dateModified = :date WHERE id = :id")
    void setUpdateTime(long id, Date date);

    static String getCompositionQuery() {
        return "SELECT " +
                "(SELECT name FROM artists WHERE id = artistId) as artist,  " +
                "(SELECT name FROM albums WHERE id = albumId) as album,  " +
                "title as title,  " +
                "fileName as fileName, " +
                "duration as duration,  " +
                "size as size,  " +
                "id as id,  " +
                "storageId as storageId,  " +
                "dateAdded as dateAdded,  " +
                "dateModified as dateModified,  " +
                "corruptionType as corruptionType  " +
                "FROM compositions";
    }
}
