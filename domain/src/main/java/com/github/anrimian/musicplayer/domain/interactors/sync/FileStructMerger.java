package com.github.anrimian.musicplayer.domain.interactors.sync;


import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.functions.BiFunction;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;

import java.util.Map;
import java.util.Set;


public class FileStructMerger {

    //move(or modify?) change
    public static <K, T> void mergeFilesMap(
            Map<K, T> localItems,
            Map<K, T> remoteItems,
            Set<K> localRemovedItems,
            Set<K> remoteRemovedItems,
            Set<K> localExistingFiles,
            Set<K> remoteExistingFiles,
            BiFunction<T, T, Boolean> itemPriorityFunction,
            Mapper<K, T> itemDataCreator,
            Callback<T> outLocalFilesToDelete,
            Callback<T> outRemoteFilesToDelete,
            Callback<T> outLocalFilesToUpload,
            Callback<T> outRemoteFileToDownload,
            BiCallback<K, T> onLocalItemAdded,
            BiCallback<K, T> omRemoteItemAdded) {

        for (Map.Entry<K, T> entry: localItems.entrySet()) {
            K key = entry.getKey();
            T localItem = entry.getValue();
            if (remoteRemovedItems.contains(key)) {

                //delete priority?
                outLocalFilesToDelete.call(localItem);
            } else  {
                onLocalItemAdded.call(key, localItem);

                if (!remoteItems.containsKey(key)) {
                    outLocalFilesToUpload.call(localItem);
                } else if (!localExistingFiles.contains(key)) {
                    outRemoteFileToDownload.call(localItem);
                }
            }
        }

        for (Map.Entry<K, T> entry : remoteItems.entrySet()) {
            K key = entry.getKey();
            T remoteItem = entry.getValue();

            T localItem = localItems.get(key);
            if (localItem == null) {
                if (localRemovedItems.contains(key)) {
                    //delete priority?
                    outRemoteFilesToDelete.call(remoteItem);
                    continue;
                } else {
                    outRemoteFileToDownload.call(remoteItem);
                }
            } else if (!remoteExistingFiles.contains(key)) {
                outLocalFilesToUpload.call(remoteItem);
            }

            //item priority fun -> return true if we need to replace local item
            if (localItem == null || itemPriorityFunction.call(localItem, remoteItem)) {
                omRemoteItemAdded.call(key, remoteItem);
            }
        }

        for (K remoteExistingFileKey: remoteExistingFiles) {
            //file was uploaded to cloud case
            if (!localItems.containsKey(remoteExistingFileKey)
                    && !remoteItems.containsKey(remoteExistingFileKey)
                    && !localRemovedItems.contains(remoteExistingFileKey)
                    && !remoteRemovedItems.contains(remoteExistingFileKey)) {
                T item = itemDataCreator.map(remoteExistingFileKey);
                onLocalItemAdded.call(remoteExistingFileKey, item);
                onLocalItemAdded.call(remoteExistingFileKey, item);
                outRemoteFileToDownload.call(item);
            }
        }
    }
}
