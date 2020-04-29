package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionMapper;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.domain.utils.Objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

public class StorageCompositionsInserter {

    private final AppDatabase appDatabase;
    private final CompositionsDao compositionsDao;
    private final CompositionsDaoWrapper compositionsDaoWrapper;
    private final FoldersDao foldersDao;
    private final ArtistsDao artistsDao;
    private final AlbumsDao albumsDao;

    public StorageCompositionsInserter(AppDatabase appDatabase,
                                       CompositionsDao compositionsDao,
                                       CompositionsDaoWrapper compositionsDaoWrapper,
                                       FoldersDao foldersDao,
                                       ArtistsDao artistsDao,
                                       AlbumsDao albumsDao) {
        this.appDatabase = appDatabase;
        this.compositionsDao = compositionsDao;
        this.compositionsDaoWrapper = compositionsDaoWrapper;
        this.foldersDao = foldersDao;
        this.artistsDao = artistsDao;
        this.albumsDao = albumsDao;
    }

    public void applyChanges(List<AddedNode> foldersToInsert,
                             List<StorageFullComposition> addedCompositions,
                             List<StorageComposition> deletedCompositions,
                             List<Change<StorageComposition, StorageFullComposition>> changedCompositions,
                             LongSparseArray<Long> movedCompositions,
                             List<Long> foldersToDelete) {
        appDatabase.runInTransaction(() -> {
            movedCompositions.putAll(insertFolders(foldersToInsert));
            applyCompositionChanges(addedCompositions,
                    deletedCompositions,
                    changedCompositions,
                    movedCompositions);
            foldersDao.deleteFolders(foldersToDelete);
        });
    }

    private void applyCompositionChanges(List<StorageFullComposition> addedCompositions,
                                         List<StorageComposition> deletedCompositions,
                                         List<Change<StorageComposition, StorageFullComposition>> changedCompositions,
                                         LongSparseArray<Long> insertedCompositionFolderMap) {
        insertCompositions(addedCompositions, insertedCompositionFolderMap);

        for (StorageComposition composition: deletedCompositions) {
            compositionsDao.delete(composition.getId());
        }

        for (Change<StorageComposition, StorageFullComposition> change: changedCompositions) {
            handleCompositionUpdate(change, insertedCompositionFolderMap);
        }
        albumsDao.deleteEmptyAlbums();
        artistsDao.deleteEmptyArtists();
//        genresDao.deleteEmptyGenres();//not working properly here. Or just not working. Check
    }

    private void handleCompositionUpdate(Change<StorageComposition, StorageFullComposition> change,
                                         LongSparseArray<Long> insertedCompositionFolderMap) {
        StorageFullComposition composition = change.getObj();
        StorageComposition oldComposition = change.getOld();
        long compositionId = oldComposition.getId();
        long compositionStorageId = oldComposition.getStorageId();

        String newArtist = composition.getArtist();
        if (!Objects.equals(newArtist, oldComposition.getArtist())) {
            compositionsDaoWrapper.updateArtist(compositionId, newArtist);
        }

        String newAlbumName = null;
        String newAlbumArtist = null;
        StorageAlbum newAlbum = composition.getStorageAlbum();
        if (newAlbum != null) {
            newAlbumName = newAlbum.getAlbum();
            newAlbumArtist = newAlbum.getArtist();
        }
        if (!Objects.equals(newAlbumName, oldComposition.getAlbum())) {
            compositionsDaoWrapper.updateAlbum(compositionId, newAlbumName);
        }
        if (!Objects.equals(newAlbumArtist, oldComposition.getAlbumArtist())) {
            compositionsDaoWrapper.updateAlbumArtist(compositionId, newAlbumArtist);
        }
        Long newFolderId = insertedCompositionFolderMap.get(compositionStorageId);
        if (!Objects.equals(oldComposition.getFolderId(), newFolderId)) {
            compositionsDao.updateFolderId(compositionId, newFolderId);
        }

        compositionsDao.update(
                composition.getTitle(),
                composition.getFileName(),
                composition.getFilePath(),
                composition.getDuration(),
                composition.getSize(),
                composition.getDateAdded(),
                composition.getDateModified(),
                composition.getId()
        );
    }

    private void insertCompositions(List<StorageFullComposition> addedCompositions,
                                    LongSparseArray<Long> insertedCompositionFolderMap) {
        //optimization with cache, ~33% faster
        Map<String, Long> artistsCache = new HashMap<>();
        Map<String, Long> albumsCache = new HashMap<>();
        compositionsDao.insert(mapList(
                addedCompositions,
                composition -> toCompositionEntity(composition, artistsCache, albumsCache, insertedCompositionFolderMap))
        );
    }

    private CompositionEntity toCompositionEntity(StorageFullComposition composition,
                                                  Map<String, Long> artistsCache,
                                                  Map<String, Long> albumsCache,
                                                  LongSparseArray<Long> insertedCompositionFolderMap) {
        String artist = composition.getArtist();
        Long artistId = getOrInsertArtist(artist, artistsCache);

        Long albumId = null;
        StorageAlbum storageAlbum = composition.getStorageAlbum();
        if (storageAlbum != null) {
            Long albumArtistId = getOrInsertArtist(storageAlbum.getArtist(), artistsCache);
            albumId = getOrInsertAlbum(storageAlbum, albumArtistId, albumsCache);
        }
        Long folderId = insertedCompositionFolderMap.get(composition.getId());
        return CompositionMapper.toEntity(composition, artistId, albumId, folderId);
    }

    private Long getOrInsertAlbum(StorageAlbum storageAlbum,
                                  Long albumArtistId,
                                  Map<String, Long> albumsCache) {
        String albumName = storageAlbum.getAlbum();

        Long albumId = albumsCache.get(albumName);
        if (albumId != null) {
            return albumId;
        }

        albumId = albumsDao.findAlbum(albumArtistId, albumName);
        if (albumId == null) {
            albumId = albumsDao.insert(new AlbumEntity(albumArtistId,
                    albumName,
                    storageAlbum.getFirstYear(),
                    storageAlbum.getLastYear()));
        }
        albumsCache.put(albumName, albumId);
        return albumId;
    }

    private Long getOrInsertArtist(String artist, Map<String, Long> artistsCache) {
        Long artistId = artistsCache.get(artist);
        if (artistId != null) {
            return artistId;
        }
        if (artist != null) {
            artistId = artistsDao.findArtistIdByName(artist);
            if (artistId == null) {
                artistId = artistsDao.insertArtist(new ArtistEntity(artist));
            }
            artistsCache.put(artist, artistId);
        }
        return artistId;
    }

    private LongSparseArray<Long> insertFolders(List<AddedNode> foldersToInsert) {
        return appDatabase.runInTransaction(() -> {
            LongSparseArray<Long> insertedCompositionFolderMap = new LongSparseArray<>();
            for (AddedNode node: foldersToInsert) {
                insertNode(node.getFolderDbId(), node.getNode(), insertedCompositionFolderMap);
            }
            return insertedCompositionFolderMap;
        });
    }

    private void insertNode(Long dbParentId,
                            FolderNode<Long> nodeToInsert,
                            LongSparseArray<Long> insertedCompositionFolderMap) {
        String name = nodeToInsert.getKeyPath();
        if (name == null) {
            return;
        }

        long id = foldersDao.insertFolder(new FolderEntity(dbParentId, name));
        for (Long compositionId : nodeToInsert.getFiles()) {
            insertedCompositionFolderMap.put(compositionId, id);
        }
        for (FolderNode<Long> node: nodeToInsert.getFolders()) {
            insertNode(id, node, insertedCompositionFolderMap);
        }
    }
}
