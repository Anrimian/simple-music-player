package com.github.anrimian.musicplayer.data.repositories.scanner.folders;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class FolderTreeBuilder<M, V> {

    private final Mapper<M, String> pathFunc;
    private final Mapper<M, V> valueFunc;

    public FolderTreeBuilder(Mapper<M, String> pathFunc, Mapper<M, V> valueFunc) {
        this.pathFunc = pathFunc;
        this.valueFunc = valueFunc;
    }

    public FolderNode<V> createFileTree(Observable<M> objectsObservable) {
        FolderNode<V> rootFolder = new FolderNode<>(null);
        objectsObservable.groupBy(pathFunc::map)
                .doOnNext(group -> group.collect(ArrayList<M>::new, List::add)
                        .map(this::toValueList)
                        .doOnSuccess(list -> addValuesToFolder(rootFolder, group.getKey(), list))
                        .subscribe())
                .subscribe();
        return rootFolder;
    }

    private List<V> toValueList(List<M> list) {
        return ListUtils.mapList(list, valueFunc::map);
    }

    private void addValuesToFolder(FolderNode<V> root,
                                   String path,
                                   List<V> values) {
        FolderNode<V> parent = getNode(root, path);
        parent.addFiles(values);
    }

    private FolderNode<V> getNode(FolderNode<V> root, String path) {
        if (TextUtils.isEmpty(path)) {
            return root;
        }

        FolderNode<V> target = root;

        String[] partialPaths = path.split("/");
        for (String partialPath : partialPaths) {
            FolderNode<V> child = target.getFolder(partialPath);
            if (child == null) {
                child = new FolderNode<>(partialPath);
                target.addFolder(child);
            }
            target = child;
        }
        return target;
    }

}
