package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import utils.TestDataProvider;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static utils.TestDataProvider.fakeStorageComposition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeTreeBuilderTest {

    private final NodeTreeBuilder nodeTreeBuilder = new NodeTreeBuilder();

    @Test
    public void createTreeFromIdMap() {
        LongSparseArray<StorageComposition> compositionsMap = new LongSparseArray<>();
        compositionsMap.put(1, new TestDataProvider.StorageLocalCompositionBuilder(1L, 11L, "test")
                .folderId(null)
                .build()
        );
        compositionsMap.put(2, new TestDataProvider.StorageLocalCompositionBuilder(2L, 22L, "test2")
                .folderId(4L)
                .build()
        );

        List<StorageFolder> folderEntities = new LinkedList<>();
        folderEntities.add(new StorageFolder(1, null, "1"));
        folderEntities.add(new StorageFolder(2, null, "2"));
        folderEntities.add(new StorageFolder(3, null, "3"));
        folderEntities.add(new StorageFolder(4, 1L, "4"));
        folderEntities.add(new StorageFolder(5, 4L, "5"));
        folderEntities.add(new StorageFolder(6, 1L, "6"));

        LocalFolderNode<Long> rootNode = nodeTreeBuilder.createTreeFromIdMap(
                folderEntities,
                compositionsMap
        );
        assertEquals(3, rootNode.getFolders().size());
        assertEquals(1, rootNode.getFiles().size());
        assert rootNode.containsFile(11L);

        LocalFolderNode<Long> nodeThree = rootNode.getFolder("1");
        assertNotNull(nodeThree);

        LocalFolderNode<Long> nodeFour = nodeThree.getFolder("4");
        assertNotNull(nodeFour);
        assertEquals(1, nodeFour.getFiles().size());
        assert nodeFour.containsFile(22L);

        LocalFolderNode<Long> nodeFive = nodeFour.getFolder("5");
        assertNotNull(nodeFive);
        LocalFolderNode<Long> nodeSix = nodeThree.getFolder("6");
        assertNotNull(nodeSix);
    }
}