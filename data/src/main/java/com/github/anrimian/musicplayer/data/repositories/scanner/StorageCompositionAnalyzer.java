package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;
import androidx.core.util.Pair;

import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.compositions.StorageCompositionsInserter;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.folder.StorageFolder;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.folders.FolderTreeBuilder;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.AddedNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.LocalFolderNode;
import com.github.anrimian.musicplayer.data.repositories.scanner.nodes.NodeTreeBuilder;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class StorageCompositionAnalyzer {

    private final CompositionsDaoWrapper compositionsDao;
    private final FoldersDaoWrapper foldersDao;
    private final StateRepository stateRepository;
    private final StorageCompositionsInserter compositionsInserter;

    private final FolderTreeBuilder<StorageFullComposition, Long> folderTreeBuilder;
    private final NodeTreeBuilder nodeTreeBuilder = new NodeTreeBuilder();
    private final FolderMerger folderMerger = new FolderMerger();

    public StorageCompositionAnalyzer(CompositionsDaoWrapper compositionsDao,
                                      FoldersDaoWrapper foldersDao,
                                      StateRepository stateRepository,
                                      StorageCompositionsInserter compositionsInserter) {
        this.compositionsDao = compositionsDao;
        this.foldersDao = foldersDao;
        this.stateRepository = stateRepository;
        this.compositionsInserter = compositionsInserter;

        folderTreeBuilder = new FolderTreeBuilder<>(
                StorageFullComposition::getRelativePath,
                StorageFullComposition::getId
        );
    }

    public synchronized void applyCompositionsData(LongSparseArray<StorageFullComposition> newCompositions) {//at the end check file path to relative path migration
        FolderNode<Long> actualFolderTree = folderTreeBuilder.createFileTree(fromSparseArray(newCompositions));

        StringBuilder sbRootPath = new StringBuilder();
        actualFolderTree = cutEmptyRootNodes(actualFolderTree, sbRootPath);
        String parentPath = TextUtils.toNullableString(sbRootPath);
        stateRepository.setRootFolderPath(parentPath);

        excludeCompositions(actualFolderTree, newCompositions);

        List<StorageFolder> storageFolders = foldersDao.getAllFolders();
        LongSparseArray<StorageComposition> currentCompositions = compositionsDao.selectAllAsStorageCompositions();

        LocalFolderNode<Long> existsFolders = nodeTreeBuilder.createTreeFromIdMap(
                storageFolders,
                currentCompositions);

        List<Long> foldersToDelete = new LinkedList<>();
        List<AddedNode> foldersToInsert = new LinkedList<>();
        LongSparseArray<Long> movedCompositions = new LongSparseArray<>();
        folderMerger.mergeFolderTrees(actualFolderTree, existsFolders, foldersToDelete, foldersToInsert, movedCompositions);

        List<StorageFullComposition> addedCompositions = new ArrayList<>();
        List<StorageComposition> deletedCompositions = new ArrayList<>();
        List<Change<StorageComposition, StorageFullComposition>> changedCompositions = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processDiffChanges(currentCompositions,
                newCompositions,
                (first, second) -> hasActualChanges(first, second) || movedCompositions.containsKey(first.getStorageId()),
                deletedCompositions::add,
                addedCompositions::add,
                (oldItem, newItem) -> changedCompositions.add(new Change<>(oldItem, newItem)));

        if (hasChanges) {
            compositionsInserter.applyChanges(foldersToInsert,
                    addedCompositions,
                    deletedCompositions,
                    changedCompositions,
                    movedCompositions,
                    foldersToDelete);
        }
    }

    private void excludeCompositions(FolderNode<Long> folderTree,
                                     LongSparseArray<StorageFullComposition> compositions) {
        String[] ignoresFolders = foldersDao.getIgnoredFolders();
        for (String ignoredFoldersPath: ignoresFolders) {

            FolderNode<Long> ignoreNode = findFolder(folderTree, ignoredFoldersPath);
            if (ignoreNode == null) {
                continue;
            }

            FolderNode<Long> parent = ignoreNode.getParentFolder();
            if (parent != null) {
                parent.removeFolder(ignoreNode.getKeyPath());
            }

            for (Long id: getAllCompositionsInNode(ignoreNode)) {
                compositions.remove(id);
            }
        }
    }

    private FolderNode<Long> cutEmptyRootNodes(FolderNode<Long> root, StringBuilder sbRootPath) {
        FolderNode<Long> found = root;

        while (isEmptyFolderNode(found)) {
            found = found.getFirstFolder();

            if (sbRootPath.length() != 0) {
                sbRootPath.append('/');
            }
            sbRootPath.append(found.getKeyPath());
        }

        return found;
    }

    private boolean isEmptyFolderNode(FolderNode<Long> node) {
        return node.getFolders().size() == 1 && node.getFiles().isEmpty();
    }

    @Nullable
    private FolderNode<Long> findFolder(FolderNode<Long> folderTree, String path) {
        FolderNode<Long> currentNode = folderTree;
        for (String partialPath: path.split("/")) {
            currentNode = currentNode.getFolder(partialPath);

            if (currentNode == null) {
                //perhaps we can implement find. Find up and down on tree.
                return null;
            }
        }
        return currentNode;
    }

    private List<Long> getAllCompositionsInNode(FolderNode<Long> parentNode) {
        LinkedList<Long> result = new LinkedList<>(parentNode.getFiles());
        for (FolderNode<Long> node: parentNode.getFolders()) {
            result.addAll(getAllCompositionsInNode(node));
        }
        return result;
    }

    private Observable<StorageFullComposition> fromSparseArray(
            LongSparseArray<StorageFullComposition> sparseArray) {
        return Observable.create(emitter -> {
            for(int i = 0, size = sparseArray.size(); i < size; i++) {
                StorageFullComposition existValue = sparseArray.valueAt(i);
                emitter.onNext(existValue);
            }
            emitter.onComplete();
        });
    }

    private boolean hasActualChanges(StorageComposition first, StorageFullComposition second) {
        if (!DateUtils.isAfter(second.getDateModified(), first.getDateModified())) {
            return false;
        }

        String newAlbumName = null;
        String newAlbumArtist = null;
        StorageAlbum newAlbum = second.getStorageAlbum();
        if (newAlbum != null) {
            newAlbumName = newAlbum.getAlbum();
            newAlbumArtist = newAlbum.getArtist();
        }

        return !(Objects.equals(first.getDateAdded(), second.getDateAdded())
                && first.getDuration() == second.getDuration()
                && Objects.equals(first.getFilePath(), second.getFilePath())
                && first.getSize() == second.getSize()
                && Objects.equals(first.getTitle(), second.getTitle())
                && Objects.equals(first.getFileName(), second.getFileName())
                && Objects.equals(first.getArtist(), second.getArtist())
                && Objects.equals(first.getAlbum(), newAlbumName)
                && Objects.equals(first.getAlbumArtist(), newAlbumArtist));
    }
}
