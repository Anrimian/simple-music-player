package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.LocalFolderNode;

import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FolderMergerTest {

    private final FolderMerger folderMerger = new FolderMerger();

    @Test
    public void mergeToEmptyTreeTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(2L);
        folder1.addFolder(folder2);

        actualFolderTree.addFolder(folder1);
        actualFolderTree.addFile(3L);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        AddedNode addedNode = foldersToInsert.get(0);
        FolderNode<Long> testFolder1 = addedNode.getNode();
        assertEquals(folder1, testFolder1);
        assert testFolder1.getFiles().contains(1L);

        FolderNode<Long> testFolder2 = testFolder1.getFolder("folder 2");
        assertNotNull(testFolder2);
        assertEquals(folder2, testFolder2);
        assert testFolder2.getFiles().contains(2L);

        assert movedFiles.containsKey(1L);
        assert movedFiles.containsKey(2L);
        assert movedFiles.containsKey(3L);
        assert movedFiles.containsValue(null);
    }

    @Test
    public void mergeSameTreesTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(2L);
        folder1.addFolder(folder2);

        actualFolderTree.addFolder(folder1);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);
        LocalFolderNode<Long> folder1Node = new LocalFolderNode<>("folder 1", 1L);
        folder1Node.addFile(1L);
        existsFolders.addFolder(folder1Node);
        LocalFolderNode<Long> folder2Node = new LocalFolderNode<>("folder 2", 2L);
        folder2Node.addFile(2L);
        folder1Node.addFolder(folder2Node);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        assertEquals(0, foldersToDelete.size());
        assertEquals(0, foldersToInsert.size());
        assertEquals(0, movedFiles.size());
    }

    @Test
    public void mergeMovedFolderTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);
        actualFolderTree.addFolder(folder1);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(2L);
        actualFolderTree.addFolder(folder2);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);
        LocalFolderNode<Long> folder1Node = new LocalFolderNode<>("folder 1", 1L);
        folder1Node.addFile(1L);
        existsFolders.addFolder(folder1Node);
        LocalFolderNode<Long> folder2Node = new LocalFolderNode<>("folder 2", 2L);
        folder2Node.addFile(2L);
        folder1Node.addFolder(folder2Node);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        assertEquals(1, foldersToDelete.size());
        assertEquals(1, foldersToInsert.size());
        assertEquals(1, movedFiles.size());

        assert foldersToDelete.contains(2L);
        assert foldersToInsert.contains(new AddedNode(null, folder2));
        assert movedFiles.containsKey(2L);
        assert movedFiles.containsValue(null);
    }

    @Test
    public void mergeMovedFileTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);
        folder1.addFile(2L);
        actualFolderTree.addFolder(folder1);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(3L);
        folder1.addFolder(folder2);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);
        LocalFolderNode<Long> localFolder1 = new LocalFolderNode<>("folder 1", 1L);
        localFolder1.addFile(1L);
        existsFolders.addFolder(localFolder1);
        LocalFolderNode<Long> localFolder2 = new LocalFolderNode<>("folder 2", 2L);
        localFolder2.addFile(3L);
        localFolder2.addFile(2L);
        localFolder1.addFolder(localFolder2);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        assertEquals(0, foldersToDelete.size());
        assertEquals(0, foldersToInsert.size());
        assertEquals(1, movedFiles.size());

        assert movedFiles.containsKey(2L);
        assert movedFiles.containsValue(1L);
    }

    @Test
    public void mergeDeletedFolderTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);
        actualFolderTree.addFolder(folder1);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);
        LocalFolderNode<Long> localFolder1 = new LocalFolderNode<>("folder 1", 1L);
        localFolder1.addFile(1L);
        existsFolders.addFolder(localFolder1);
        LocalFolderNode<Long> localFolder2 = new LocalFolderNode<>("folder 2", 2L);
        localFolder2.addFile(2L);
        localFolder2.addFile(3L);
        localFolder1.addFolder(localFolder2);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        assertEquals(1, foldersToDelete.size());
        assert foldersToDelete.contains(2L);

        assertEquals(0, foldersToInsert.size());
        assertEquals(0, movedFiles.size());
    }

    @Test
    public void addFileTestTest() {
        FolderNode<Long> actualFolderTree = new FolderNode<>(null);
        FolderNode<Long> folder1 = new FolderNode<>("folder 1");
        folder1.addFile(1L);

        FolderNode<Long> folder2 = new FolderNode<>("folder 2");
        folder2.addFile(2L);
        folder2.addFile(3L);
        folder1.addFolder(folder2);

        actualFolderTree.addFolder(folder1);

        LocalFolderNode<Long> existsFolders = new LocalFolderNode<>(null, null);
        LocalFolderNode<Long> folder1Node = new LocalFolderNode<>("folder 1", 1L);
        folder1Node.addFile(1L);
        existsFolders.addFolder(folder1Node);
        LocalFolderNode<Long> folder2Node = new LocalFolderNode<>("folder 2", 2L);
        folder2Node.addFile(2L);
        folder1Node.addFolder(folder2Node);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedFiles = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedFiles);

        assertEquals(0, foldersToDelete.size());
        assertEquals(0, foldersToInsert.size());
        assertEquals(1, movedFiles.size());

        assert movedFiles.containsKey(3L);
        assert movedFiles.containsValue(2L);
    }
}