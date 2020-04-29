package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.LocalFolderNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FolderMerger {

    public void mergeFolderTrees(FolderNode<Long> actualFolderNode,
                                 LocalFolderNode<Long> existsFoldersNode,
                                 List<Long> outFoldersToDelete,
                                 List<AddedNode> outFoldersToInsert,
                                 LongSparseArray<Long> outAddedFiles) {
        for (Long file: actualFolderNode.getFiles()) {
            if (!existsFoldersNode.containsFile(file)) {
                outAddedFiles.put(file, existsFoldersNode.getId());
            }
        }
        for (LocalFolderNode<Long> existFolder : existsFoldersNode.getFolders()) {
            String key = existFolder.getKeyPath();

            FolderNode<Long> actualFolder = actualFolderNode.getFolder(key);
            if (actualFolder == null) {
                outFoldersToDelete.add(existFolder.getId());
            }
        }
        for (FolderNode<Long> actualFolder : actualFolderNode.getFolders()) {
            String key = actualFolder.getKeyPath();

            LocalFolderNode<Long> existFolder = existsFoldersNode.getFolder(key);
            if (existFolder == null) {
                Long parentId = existsFoldersNode.getId();

                AddedNode addedNode = new AddedNode(parentId, actualFolder);
                outFoldersToInsert.add(addedNode);

                LongSparseArray<Long> affectedFiles = getAllFilesInNode(actualFolder);
                outAddedFiles.putAll(affectedFiles);
            } else {
                mergeFolderTrees(actualFolder, existFolder, outFoldersToDelete, outFoldersToInsert, outAddedFiles);
            }
        }
    }

    private LongSparseArray<Long> getAllFilesInNode(FolderNode<Long> parentNode) {
        LongSparseArray<Long> result = new LongSparseArray<>();
        for (Long file: parentNode.getFiles()) {
            result.put(file, null);
        }

        for (FolderNode<Long> node: parentNode.getFolders()) {
            result.putAll(getAllFilesInNode(node));
        }
        return result;
    }

}
