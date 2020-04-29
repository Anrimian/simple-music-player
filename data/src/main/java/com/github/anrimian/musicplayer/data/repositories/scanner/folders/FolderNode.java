package com.github.anrimian.musicplayer.data.repositories.scanner.folders;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FolderNode<V> {

    private final LinkedHashMap<String, FolderNode<V>> folders = new LinkedHashMap<>();

    @Nullable
    private String key;
    private List<V> files = new LinkedList<>();

    @Nullable
    private FolderNode<V> parent;

    public FolderNode(@Nullable String key) {
        this.key = key;
    }

    @Nullable
    public FolderNode<V> getParentFolder() {
        return parent;
    }

    @Nonnull
    public Collection<FolderNode<V>> getFolders() {
        return folders.values();
    }

    @Nullable
    public String getKeyPath() {
        return key;
    }

    public List<V> getFiles() {
        return files;
    }

    public void addFile(V file) {
        files.add(file);
    }

    public void addFiles(List<V> files) {
        this.files.addAll(files);
    }

    public void addFolders(List<FolderNode<V>> newNodes) {
        for (FolderNode<V> newNode : newNodes) {
            newNode.parent = this;
            folders.put(newNode.getKeyPath(), newNode);
        }
    }

    public void addFolder(FolderNode<V> node) {
        node.parent = this;
        folders.put(node.getKeyPath(), node);
    }

    public void removeFolders(List<String> keys) {
        List<FolderNode<V>> removedNodes = new ArrayList<>();
        for (String key: keys) {
            FolderNode<V> removedNode = folders.remove(key);
            if (removedNode != null) {
                removedNodes.add(removedNode);
            }
        }
        if (!removedNodes.isEmpty()) {
            clearEmptyNodeIfNeed(this);
        }
    }

    public void removeFolder(String key) {
        FolderNode<V> removedNode = folders.remove(key);
        if (removedNode != null) {
            clearEmptyNodeIfNeed(this);
        }
    }

    @Nullable
    public FolderNode<V> getFolder(String key) {
        return folders.get(key);
    }

    public FolderNode<V> getFirstFolder() {
        return folders.entrySet().iterator().next().getValue();
    }

    public void setParent(@Nullable FolderNode<V> parent) {
        this.parent = parent;
    }

    private void updateChildKey(FolderNode<V> node, String newKey) {
        folders.remove(node.getKeyPath());
        folders.put(newKey, node);
    }

    private void clearEmptyNodeIfNeed(FolderNode<V> node) {
        if (node.getFolders().isEmpty()) {
            FolderNode<V> parent = node.getParentFolder();
            if (parent != null) {
                parent.removeFolder(node.getKeyPath());
                clearEmptyNodeIfNeed(parent);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Node{" +
                "key=" + key +
                ", files=" + files +
                ", nodesCount=" + folders.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderNode<?> that = (FolderNode<?>) o;

        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
