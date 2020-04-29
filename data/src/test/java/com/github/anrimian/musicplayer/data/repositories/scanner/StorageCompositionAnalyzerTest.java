package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

import utils.TestDataProvider.StorageCompositionBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static utils.TestDataProvider.fakeStorageComposition;
import static utils.TestDataProvider.fakeStorageFullComposition;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageCompositionAnalyzerTest {

    private CompositionsDaoWrapper compositionsDao = mock(CompositionsDaoWrapper.class);
    private FoldersDaoWrapper foldersDao = mock(FoldersDaoWrapper.class);
    private StateRepository stateRepository = mock(StateRepository.class);
    private StorageCompositionsInserter compositionsInserter = mock(StorageCompositionsInserter.class);

    private StorageCompositionAnalyzer analyzer;
    
    @Before
    public void setUp() {
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(new LongSparseArray<>());

        when(foldersDao.getIgnoredFolders()).thenReturn(new String[0]);

        analyzer = new StorageCompositionAnalyzer(compositionsDao, foldersDao, stateRepository, compositionsInserter);
    }

    @Test
    public void changeDatabaseTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1"));
        currentCompositions.put(2, fakeStorageComposition(2, "music-2"));
        currentCompositions.put(3, fakeStorageComposition(3, "music-3"));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, fakeStorageFullComposition(1, "music-1"));
        StorageFullComposition changedComposition = new StorageCompositionBuilder(3, "changed composition")
                .createDate(1L)
                .modifyDate(10000L)
                .build();
        newCompositions.put(3L, changedComposition);
        newCompositions.put(4, fakeStorageFullComposition(4, "music-4"));

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter).applyChanges(
                any(),
                eq(asList(fakeStorageFullComposition(4, "music-4"))),//new
                eq(asList(fakeStorageComposition(2, "music-2"))),//removed
                eq(asList(new Change<>(fakeStorageComposition(3, "music-3"), changedComposition))),
                any(),
                any());
    }

    @Test
    public void testUpdateTimeChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1000));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageCompositionBuilder(1, "new title")
                .createDate(1L)
                .modifyDate(1000L)
                .build();
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter, never()).applyChanges(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1000), changedComposition))),
                any(),
                any());
    }

    @Test
    public void testUpdateArtistChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition("new artist",
                null,
                "fileName",
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                null);
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq("test"));
        verify(compositionsInserter).applyChanges(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1), changedComposition))),
                any(),
                any());
    }

    @Test
    public void testUpdateAlbumChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        map.put(1L, fakeStorageComposition(1L, "test", 1, 1));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition(null,
                null,
                "fileName",
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                new StorageAlbum(1, "test album", null, 0, 1));
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq("test"));
        verify(compositionsInserter).applyChanges(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(fakeStorageComposition(1L, "test", 1, 1), changedComposition))),
                any(),
                any());
    }

    @Test
    public void testUpdateAlbumArtistChange() {
        LongSparseArray<StorageComposition> map = new LongSparseArray<>();
        StorageComposition oldComposition = new StorageComposition(null,
                "album artist",
                null,
                "fileName",
                "test album",
                "test",
                0,
                0,
                1L,
                1L,
                null,
                new Date(1),
                new Date(1));
        map.put(1L, oldComposition);
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(map);

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        StorageFullComposition changedComposition = new StorageFullComposition(null,
                null,
                "fileName",
                "test",
                "test",
                0,
                0,
                1L,
                new Date(1),
                new Date(1000),
                new StorageAlbum(1, "test album", "new album artist", 0, 1));
        newCompositions.put(1L, changedComposition);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq("test"));
        verify(compositionsInserter).applyChanges(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(oldComposition, changedComposition))),
                any(),
                any());
    }

    @Test
    public void excludeFoldersTest() {
        String[] excludedFolders = {"music/wazap", "rubbish"};

        when(foldersDao.getIgnoredFolders()).thenReturn(excludedFolders);

        StorageFullComposition expectedComposition = new StorageCompositionBuilder(4, "music-4")
                .relativePath("0/etc/sdcard")
                .build();

        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, new StorageCompositionBuilder(1, "music-1").relativePath("0/etc/sdcard/music/wazap").build());
        newCompositions.put(2, new StorageCompositionBuilder(2, "music-2").relativePath("0/etc/sdcard/music/wazap").build());
        newCompositions.put(3, new StorageCompositionBuilder(3, "music-3").relativePath("0/etc/sdcard/music/wazap").build());
        newCompositions.put(4, expectedComposition);
        newCompositions.put(5, new StorageCompositionBuilder(5, "music-5").relativePath("0/etc/sdcard/rubbish").build());

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq("0/etc/sdcard"));
        verify(compositionsInserter).applyChanges(
                any(),
                eq(asList(expectedComposition)),
                eq(emptyList()),
                eq(emptyList()),
                any(),
                any());
    }

    @Test
    public void insertFolderTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        StorageFullComposition c1 = new StorageCompositionBuilder(1, "music-1").relativePath("music").build();
        StorageFullComposition c2 = new StorageCompositionBuilder(2, "music-2").relativePath("music/new").build();
        StorageFullComposition c3 = new StorageCompositionBuilder(3, "music-3").relativePath("").build();
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(2, c2);
        newCompositions.put(3, c3);

        analyzer.applyCompositionsData(newCompositions);

        List<AddedNode> expectedNodesToInsert = new ArrayList<>();
        FolderNode<Long> node = new FolderNode<>("music");
        node.addFolder(new FolderNode<>("new"));
        expectedNodesToInsert.add(new AddedNode(null, node));

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter).applyChanges(
                eq(expectedNodesToInsert),
                eq(asList(c1, c2, c3)),
                eq(emptyList()),
                eq(emptyList()),
                any(),
                eq(emptyList()));
    }

    @Test
    public void mergeSameFoldersTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1", 1L));
        currentCompositions.put(2, fakeStorageComposition(2, "music-2", 2L));
        currentCompositions.put(3, fakeStorageComposition(3, "music-3"));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        List<StorageFolder> folders = new LinkedList<>();
        folders.add(new StorageFolder(1L, null, "music"));
        folders.add(new StorageFolder(2L, 1L, "new"));
        when(foldersDao.getAllFolders()).thenReturn(folders);

        StorageFullComposition c1 = new StorageCompositionBuilder(1, "music-1").relativePath("music").build();
        StorageFullComposition c2 = new StorageCompositionBuilder(2, "music-2").relativePath("music/new").build();
        StorageFullComposition c3 = new StorageCompositionBuilder(3, "music-3").relativePath("").build();
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(2, c2);
        newCompositions.put(3, c3);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter, never()).applyChanges(any(),
                any(),
                any(),
                any(),
                any(),
                any());
    }

    @Test
    public void mergeMovedFoldersTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1", 1L));
        StorageComposition composition2 = fakeStorageComposition(2, "music-2", 2L);
        currentCompositions.put(2, composition2);
        currentCompositions.put(3,  fakeStorageComposition(3, "music-3"));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        List<StorageFolder> folders = new LinkedList<>();
        folders.add(new StorageFolder(1L, null, "music"));
        folders.add(new StorageFolder(2L, 1L, "new"));
        when(foldersDao.getAllFolders()).thenReturn(folders);

        StorageFullComposition c1 = new StorageCompositionBuilder(1, "music-1").relativePath("music").build();
        StorageFullComposition c2 = new StorageCompositionBuilder(2, "music-2").relativePath("new").build();
        StorageFullComposition c3 = new StorageCompositionBuilder(3, "music-3").relativePath("").build();
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(2, c2);
        newCompositions.put(3, c3);

        analyzer.applyCompositionsData(newCompositions);

        List<AddedNode> expectedNodesToInsert = new ArrayList<>();
        FolderNode<Long> node = new FolderNode<>("new");
        node.addFile(2L);
        expectedNodesToInsert.add(new AddedNode(null, node));

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter).applyChanges(
                eq(expectedNodesToInsert),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(composition2, c2))),
                any(),
                eq(asList(2L)));
    }

    @Test
    public void mergeMovedFilesTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1", 1L));
        StorageComposition composition2 = fakeStorageComposition(2, "music-2", 2L);
        currentCompositions.put(2, composition2);
        currentCompositions.put(3, fakeStorageComposition(3, "music-3", null));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        List<StorageFolder> folders = new LinkedList<>();
        folders.add(new StorageFolder(1L, null, "music"));
        folders.add(new StorageFolder(2L, 1L, "new"));
        when(foldersDao.getAllFolders()).thenReturn(folders);

        StorageFullComposition c1 = new StorageCompositionBuilder(1, "music-1").relativePath("music").build();
        StorageFullComposition c2 = new StorageCompositionBuilder(2, "music-2").relativePath("new").build();
        StorageFullComposition c3 = new StorageCompositionBuilder(3, "music-3").relativePath("").build();
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(2, c2);
        newCompositions.put(3, c3);

        analyzer.applyCompositionsData(newCompositions);

        List<AddedNode> expectedNodesToInsert = new ArrayList<>();
        FolderNode<Long> node = new FolderNode<>("new");
        expectedNodesToInsert.add(new AddedNode(null, node));

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter).applyChanges(
                eq(expectedNodesToInsert),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(composition2, c2))),
                any(),
                any());
    }

    @Test
    public void mergeDeletedFoldersTest() {
        LongSparseArray<StorageComposition> currentCompositions = new LongSparseArray<>();
        currentCompositions.put(1, fakeStorageComposition(1, "music-1", 1L));
        StorageComposition composition2 = fakeStorageComposition(2, "music-2", 2L);
        currentCompositions.put(2, composition2);
        currentCompositions.put(3,  fakeStorageComposition(3, "music-3"));
        when(compositionsDao.selectAllAsStorageCompositions()).thenReturn(currentCompositions);

        List<StorageFolder> folders = new LinkedList<>();
        folders.add(new StorageFolder(1L, null, "music"));
        folders.add(new StorageFolder(2L, 1L, "new"));
        when(foldersDao.getAllFolders()).thenReturn(folders);

        StorageFullComposition c1 = new StorageCompositionBuilder(1, "music-1").relativePath("music").build();
        StorageFullComposition c3 = new StorageCompositionBuilder(3, "music-3").relativePath("").build();
        LongSparseArray<StorageFullComposition> newCompositions = new LongSparseArray<>();
        newCompositions.put(1, c1);
        newCompositions.put(3, c3);

        analyzer.applyCompositionsData(newCompositions);

        verify(stateRepository).setRootFolderPath(eq(null));
        verify(compositionsInserter).applyChanges(
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(composition2)),
                eq(emptyList()),
                any(),
                eq(asList(2L)));
    }

}