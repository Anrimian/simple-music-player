package com.github.anrimian.musicplayer.data.database.dao.compositions;

import android.content.Context;

import androidx.collection.LongSparseArray;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import utils.TestDataProvider;

import static org.junit.Assert.assertEquals;
import static utils.TestDataProvider.composition;

public class StorageCompositionsInserterTest {

    private AppDatabase db;
    private CompositionsDao compositionsDao;
    private CompositionsDaoWrapper compositionsDaoWrapper;
    private FoldersDao foldersDao;
    private ArtistsDao artistsDao;
    private AlbumsDao albumsDao;

    private StorageCompositionsInserter inserter;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        artistsDao = db.artistsDao();
        albumsDao = db.albumsDao();
        foldersDao = db.foldersDao();
        compositionsDaoWrapper = new CompositionsDaoWrapper(db,
                artistsDao,
                compositionsDao,
                albumsDao);

        inserter = new StorageCompositionsInserter(db,
                compositionsDao,
                compositionsDaoWrapper,
                foldersDao,
                artistsDao,
                albumsDao);
    }

    @Test
    public void applyCompositionMoveChanges() {
        long folderId = foldersDao.insertFolder(new FolderEntity(null, "test folder 1"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title", folderId));

        List<AddedNode> foldersToInsert = new LinkedList<>();
        FolderNode<Long> newFolderNode = new FolderNode<>("test folder 2");
        newFolderNode.addFile(compositionId);
        foldersToInsert.add(new AddedNode(null, newFolderNode));

        List<Long> foldersToDelete = new LinkedList<>();
        foldersToDelete.add(folderId);

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();
        changedCompositions.add(new Change<>(
                TestDataProvider.fakeStorageComposition(compositionId, "test title", folderId),
                TestDataProvider.fakeStorageFullComposition(1L, "test title", "test folder 2")
        ));

        inserter.applyChanges(foldersToInsert,
                addedCompositions,
                deletedCompositions,
                changedCompositions,
                new LongSparseArray<>(),
                foldersToDelete);

        List<StorageFolder> folders = foldersDao.getAllFolders();
        assertEquals(1, folders.size());
        StorageFolder folder = folders.get(0);
        assertEquals("test folder 2", folder.getName());
    }

    @Test
    public void applyCompositionMoveToExistFolderChanges() {
        long folder1Id = foldersDao.insertFolder(new FolderEntity(null, "test folder 1"));
        long folder2Id = foldersDao.insertFolder(new FolderEntity(null, "test folder 2"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title", folder1Id));

        List<AddedNode> foldersToInsert = new LinkedList<>();
        List<Long> foldersToDelete = new LinkedList<>();
        foldersToDelete.add(folder1Id);

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();
        changedCompositions.add(new Change<>(
                TestDataProvider.fakeStorageComposition(compositionId, "test title", folder1Id),
                TestDataProvider.fakeStorageFullComposition(1L, "test title", "test folder 2")
        ));

        inserter.applyChanges(foldersToInsert,
                addedCompositions,
                deletedCompositions,
                changedCompositions,
                AndroidCollectionUtils.sparseArrayOf(compositionId, folder2Id),
                foldersToDelete);

        List<StorageFolder> folders = foldersDao.getAllFolders();
        assertEquals(1, folders.size());
        StorageFolder folder = folders.get(0);
        assertEquals("test folder 2", folder.getName());
    }
}