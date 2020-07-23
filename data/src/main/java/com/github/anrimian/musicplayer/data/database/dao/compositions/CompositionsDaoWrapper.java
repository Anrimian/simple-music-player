package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.collection.LongSparseArray;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.models.exceptions.CompositionNotFoundException;
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.order.Order;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static com.github.anrimian.musicplayer.data.database.utils.DatabaseUtils.getSearchArgs;

public class CompositionsDaoWrapper {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;

    public CompositionsDaoWrapper(AppDatabase appDatabase,
                                  ArtistsDao artistsDao,
                                  CompositionsDao compositionsDao,
                                  AlbumsDao albumsDao) {
        this.appDatabase = appDatabase;
        this.artistsDao = artistsDao;
        this.compositionsDao = compositionsDao;
        this.albumsDao = albumsDao;
    }

    public Observable<FullComposition> getCompositionObservable(long id) {
        return compositionsDao.getCompositionObservable(id)
                .takeWhile(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public Observable<List<Composition>> getAllObservable(Order order,
                                                          @Nullable String searchText) {
        String query = CompositionsDao.getCompositionQuery();
        query += getSearchQuery();
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query, getSearchArgs(searchText, 3));
        return compositionsDao.getAllObservable(sqlQuery);
    }

    public Observable<List<Composition>> getCompositionsInFolderObservable(Long folderId,
                                                                           Order order,
                                                                           @Nullable String searchText) {
        StringBuilder query = new StringBuilder(CompositionsDao.getCompositionQuery());
        String searchQuery = getSearchQuery();
        query.append(searchQuery);
        query.append(" AND ");
        query.append("folderId = ");
        query.append(folderId);
        query.append(" OR (folderId IS NULL AND ");
        query.append(folderId);
        query.append(" IS NULL)");
        query.append(getOrderQuery(order));
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query.toString(), getSearchArgs(searchText, 3));
        return compositionsDao.getAllInFolderObservable(sqlQuery);
    }

    public List<Composition> getAllCompositionsInFolder(Long parentFolderId) {
        String query = FoldersDao.getRecursiveFolderQuery(parentFolderId);
        query += CompositionsDao.getCompositionQuery();
        query += " WHERE folderId IN (SELECT childFolderId FROM allChildFolders) ";
        query += "OR folderId = ";
        query += parentFolderId;
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.executeQuery(sqlQuery);
    }

    public List<Composition> getCompositionsInFolder(Long parentFolderId, Order order) {
        String query = CompositionsDao.getCompositionQuery();
        query += " WHERE folderId = ";
        query += parentFolderId;
        query += " OR (folderId IS NULL AND ";
        query += parentFolderId;
        query += " IS NULL)";
        query += getOrderQuery(order);
        SimpleSQLiteQuery sqlQuery = new SimpleSQLiteQuery(query);
        return compositionsDao.executeQuery(sqlQuery);
    }

    public LongSparseArray<StorageComposition> selectAllAsStorageCompositions() {
        return AndroidCollectionUtils.mapToSparseArray(compositionsDao.selectAllAsStorageCompositions(),
                StorageComposition::getStorageId);
    }

    public long getStorageId(long compositionId) {
        Long storageId = compositionsDao.getStorageId(compositionId);
        if (storageId == null) {
            throw new CompositionNotFoundException();
        }
        return storageId;
    }

    public void delete(long id) {
        compositionsDao.delete(id);
    }

    public void deleteAll(List<Long> ids) {
        compositionsDao.delete(ids);
    }

    public void updateFolderId(long id, Long folderId) {
        compositionsDao.updateFolderId(id, folderId);
    }

    public void updateFilePath(long id, String filePath) {
        compositionsDao.updateFilePath(id, filePath);
    }

    public void updateFilesPath(List<FilePathComposition> compositions) {
        appDatabase.runInTransaction(() -> {
            for (FilePathComposition composition: compositions) {
                compositionsDao.updateFilePath(composition.getId(), composition.getFilePath());
            }
        });
    }

    public void updateAlbum(long compositionId, @Nullable String albumName) {
        appDatabase.runInTransaction(() -> {

            Long artistId = null;
            Long existsAlbumId = compositionsDao.getAlbumId(compositionId);
            if (existsAlbumId != null) {
                artistId = albumsDao.getArtistId(existsAlbumId);
            }
            if (artistId == null) {
                artistId = compositionsDao.getArtistId(compositionId);
            }

            // find new album by artist and name from albums
            Long albumId = albumsDao.findAlbum(artistId, albumName);

            // if album not exists - create album
            if (albumId == null && albumName != null) {
                //single crash here
                //irrelevant artist id?
                //already existing album? - no
                //non unique albumName and artist id?
                albumId = albumsDao.insert(new AlbumEntity(artistId, albumName, 0, 0));
            }

            // set new albumId
            Long oldAlbumId = compositionsDao.getAlbumId(compositionId);
            compositionsDao.updateAlbum(compositionId, albumId);
            compositionsDao.setUpdateTime(compositionId, new Date());

            if (oldAlbumId != null) {
                albumsDao.deleteEmptyAlbum(oldAlbumId);
            }
        });
    }

    public void updateArtist(long id, String authorName) {
        appDatabase.runInTransaction(() -> {
            // 1) find new artist by name from artists
            Long artistId = artistsDao.findArtistIdByName(authorName);

            // 2) if artist not exists - create artist
            if (artistId == null && authorName != null) {
                artistId = artistsDao.insertArtist(new ArtistEntity(authorName));
            }
            // 3) set new artistId
            Long oldArtistId = compositionsDao.getArtistId(id);
            compositionsDao.updateArtist(id, artistId);
            compositionsDao.setUpdateTime(id, new Date());

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }
        });
    }

    public void updateAlbumArtist(long id, String artistName) {
        appDatabase.runInTransaction(() -> {
            //find album
            Long albumId = compositionsDao.getAlbumId(id);
            if (albumId == null) {
                return;
            }
            // 1) find new artist by name from artists
            Long artistId = artistsDao.findArtistIdByName(artistName);

            // 2) if artist not exists - create artist
            if (artistId == null && artistName != null) {
                artistId = artistsDao.insertArtist(new ArtistEntity(artistName));
            }

            AlbumEntity albumEntity = albumsDao.getAlbumEntity(albumId);
            Long oldArtistId = albumEntity.getArtistId();

            //find new album with author id and name
            Long newAlbumId = albumsDao.findAlbum(artistId, albumEntity.getName());
            //if not exists, create

            if (newAlbumId == null) {
                newAlbumId = albumsDao.insert(new AlbumEntity(
                        artistId,
                        albumEntity.getName(),
                        albumEntity.getFirstYear(),
                        albumEntity.getLastYear()
                ));
            }
            //set new album to composition
            compositionsDao.setAlbumId(id, newAlbumId);
            compositionsDao.setUpdateTime(id, new Date());

            //if album is empty, delete
            albumsDao.deleteEmptyAlbum(albumId);

            // 4) if OLD artist exists and has no references - delete him
            if (oldArtistId != null) {
                artistsDao.deleteEmptyArtist(oldArtistId);
            }

        });
    }

    public void updateTitle(long id, String title) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.updateTitle(id, title);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateLyrics(long id, String text) {
        appDatabase.runInTransaction(() -> {
            compositionsDao.updateLyrics(id, text);
            compositionsDao.setUpdateTime(id, new Date());
        });
    }

    public void updateModifyTime(long id, Date date) {
        compositionsDao.setUpdateTime(id, date);
    }

    public void updateModifyTimeAndSize(long id, long size, Date date) {
        compositionsDao.setModifyTimeAndSize(id, size, date);
    }

    public void updateCompositionFileName(long id, String fileName) {
        compositionsDao.updateCompositionFileName(id, fileName);
    }

    public void setCorruptionType(CorruptionType corruptionType, long id) {
        compositionsDao.setCorruptionType(corruptionType, id);
    }

    private String getOrderQuery(Order order) {
        StringBuilder orderQuery = new StringBuilder(" ORDER BY ");
        switch (order.getOrderType()) {
            case ALPHABETICAL: {
                orderQuery.append("title");
                break;
            }
            case ADD_TIME: {
                orderQuery.append("dateAdded");
                break;
            }
            case SIZE: {
                orderQuery.append("size");
                break;
            }
            case DURATION: {
                orderQuery.append("duration");
                break;
            }
            default: throw new IllegalStateException("unknown order type" + order);
        }
        orderQuery.append(" ");
        orderQuery.append(order.isReversed()? "DESC" : "ASC");
        return orderQuery.toString();
    }

    private String getSearchQuery() {
        return " WHERE (? IS NULL OR title LIKE ? OR (artist NOTNULL AND artist LIKE ?))";
    }
}
