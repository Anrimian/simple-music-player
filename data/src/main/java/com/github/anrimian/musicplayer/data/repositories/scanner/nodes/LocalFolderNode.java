package com.github.anrimian.musicplayer.data.repositories.scanner.nodes;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LocalFolderNode<V> {

    private final LinkedHashMap<String, LocalFolderNode<V>> folders = new LinkedHashMap<>();

    @Nullable
    private Long id;
    @Nullable
    private String key;
    private LinkedHashSet<V> files = new LinkedHashSet<>();

    @Nullable
    private LocalFolderNode<V> parent;

    public LocalFolderNode(@Nullable String key, @Nullable Long id) {
        this.key = key;
        this.id = id;
    }

    @Nullable
    public LocalFolderNode<V> getParentFolder() {
        return parent;
    }

    @Nonnull
    public Collection<LocalFolderNode<V>> getFolders() {
        return folders.values();
    }

    @Nullable
    public String getKeyPath() {
        return key;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public Collection<V> getFiles() {
        return files;
    }

    public boolean containsFile(V file) {
        return files.contains(file);
    }

    public void addFile(V file) {
        files.add(file);
    }

    public void addFiles(List<V> files) {
        this.files.addAll(files);
    }

    public void addFolders(List<LocalFolderNode<V>> newNodes) {
        for (LocalFolderNode<V> newNode : newNodes) {
            newNode.parent = this;
            folders.put(newNode.getKeyPath(), newNode);
        }
    }

    public void addFolder(LocalFolderNode<V> node) {
        node.parent = this;
        folders.put(node.getKeyPath(), node);
    }

    public void removeFolders(List<String> keys) {
        List<LocalFolderNode<V>> removedNodes = new ArrayList<>();
        for (String key: keys) {
            LocalFolderNode<V> removedNode = folders.remove(key);
            if (removedNode != null) {
                removedNodes.add(removedNode);
            }
        }
        if (!removedNodes.isEmpty()) {
            clearEmptyNodeIfNeed(this);
        }
    }

    public void removeFolder(String key) {
        LocalFolderNode<V> removedNode = folders.remove(key);
        if (removedNode != null) {
            clearEmptyNodeIfNeed(this);
        }
    }

    @Nullable
    public LocalFolderNode<V> getFolder(String key) {
        return folders.get(key);
    }

    public LocalFolderNode<V> getFirstFolder() {
        return folders.entrySet().iterator().next().getValue();
    }

    public void setParent(@Nullable LocalFolderNode<V> parent) {
        this.parent = parent;
    }

    private void updateChildKey(LocalFolderNode<V> node, String newKey) {
        folders.remove(node.getKeyPath());
        folders.put(newKey, node);
    }

    private void clearEmptyNodeIfNeed(LocalFolderNode<V> node) {
        if (node.getFolders().isEmpty()) {
            LocalFolderNode<V> parent = node.getParentFolder();
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

        LocalFolderNode<?> that = (LocalFolderNode<?>) o;

        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
