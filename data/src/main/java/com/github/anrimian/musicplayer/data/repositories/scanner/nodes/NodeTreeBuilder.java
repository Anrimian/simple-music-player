package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;

import java.util.LinkedList;
import java.util.List;

public class NodeTreeBuilder {

    public LocalFolderNode<Long> createTreeFromIdMap(List<StorageFolder> folders,
                                                     LongSparseArray<StorageComposition> filesMap) {
        LongSparseArray<List<Long>> folderIdMap = buildFolderIdMap(filesMap);

        LongSparseArray<List<StorageFolder>> idMap = new LongSparseArray<>();

        LocalFolderNode<Long> rootNode = new LocalFolderNode<>(null, null);
        List<Long> files = folderIdMap.get(0);
        if (files != null) {
            rootNode.addFiles(files);
        }

        for (StorageFolder folder: folders) {
            Long parentId = folder.getParentId();
            if (parentId == null) {
                rootNode.addFolder(newLocalFolder(folder, folderIdMap));
            } else {
                List<StorageFolder> childList = idMap.get(parentId);
                if (childList == null) {
                    childList = new LinkedList<>();
                    idMap.put(parentId, childList);
                }
                childList.add(folder);
            }
        }

        for (LocalFolderNode<Long> childNode: rootNode.getFolders()) {
            fillNode(childNode, idMap, folderIdMap);
        }

        if (!idMap.isEmpty()) {
            throw new IllegalStateException("found missed folders");
        }

        return rootNode;
    }

    private LongSparseArray<List<Long>> buildFolderIdMap(LongSparseArray<StorageComposition> filesMap) {
        LongSparseArray<List<Long>> folderIdMap = new LongSparseArray<>();

        for(int i = 0, size = filesMap.size(); i < size; i++) {
            StorageComposition file = filesMap.valueAt(i);
            Long folderId = file.getFolderId();
            if (folderId == null) {
                folderId = 0L;
            }
            List<Long> childList = folderIdMap.get(folderId);
            if (childList == null) {
                childList = new LinkedList<>();
                folderIdMap.put(folderId, childList);
            }
            childList.add(file.getStorageId());
        }
        return folderIdMap;
    }

    private void fillNode(LocalFolderNode<Long> targetNode,
                          LongSparseArray<List<StorageFolder>> parentIdMap,
                          LongSparseArray<List<Long>> folderIdMap) {
        Long id = targetNode.getId();
        if (id == null) {
            throw new IllegalStateException("try to access folder with null value");
        }
        List<StorageFolder> childList = parentIdMap.get(id);
        if (childList != null) {
            for (StorageFolder folder: childList) {
                LocalFolderNode<Long> node = newLocalFolder(folder, folderIdMap);
                targetNode.addFolder(node);
                fillNode(node, parentIdMap, folderIdMap);
            }
        }
        parentIdMap.remove(id);
    }

    private LocalFolderNode<Long> newLocalFolder(StorageFolder folder,
                                                 LongSparseArray<List<Long>> folderIdMap) {
        long id = folder.getId();
        LocalFolderNode<Long> node = new LocalFolderNode<>(folder.getName(), id);//and fill with files
        List<Long> files = folderIdMap.get(id);
        if (files != null) {
            node.addFiles(files);
        }
        return node;
    }

}
