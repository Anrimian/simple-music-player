package com.github.anrimian.musicplayer.domain.interactors.sync;


import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.functions.BiFunction;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;
import com.github.anrimian.musicplayer.domain.utils.functions.TripleCallback;

import java.util.Map;
import java.util.Set;


public class StructMerger {

    //delete file from remote case?
    public static <K, T, R> void mergeFilesMap(
            Map<K, T> localItems,
            Map<K, T> remoteItems,
            Map<K, R> localRemovedItems,
            Map<K, R> remoteRemovedItems,
            Set<K> localExistingFiles,
            Set<K> remoteExistingFiles,
            BiFunction<T, T, Boolean> changeInspector,//we really need it?
            BiFunction<T, T, Boolean> itemPriorityFunction,
            BiFunction<T, R, Boolean> removedItemPriorityFunction,
            Mapper<K, T> itemDataCreator,
            Callback<T> outLocalFileToDelete,
            Callback<T> outRemoteFileToDelete,
            Callback<T> outLocalFileToUpload,
            BiCallback<T, Boolean> outRemoteFileToDownload,
            BiCallback<K, T> onLocalItemAdded,
            BiCallback<K, T> onLocalItemRemoved,
            TripleCallback<K, T, T> onLocalItemChanged,
            BiCallback<K, T> onRemoteItemAdded,
            BiCallback<K, T> omRemoteItemRemoved,
            TripleCallback<K, T, T> onRemoteItemChanged,
            BiCallback<K, R> onLocalRemovedItemNotActual,
            BiCallback<K, R> onRemoteRemovedItemNotActual) {

        for (Map.Entry<K, T> entry: localItems.entrySet()) {
            K localKey = entry.getKey();
            T localItem = entry.getValue();
            T remoteItem = remoteItems.get(localKey);

            //delete local
            R remoteRemovedItem = remoteRemovedItems.get(localKey);
            if (remoteRemovedItem != null) {
                if (removedItemPriorityFunction.call(localItem, remoteRemovedItem)) {
                    onLocalItemRemoved.call(localKey, localItem);
                    if (localExistingFiles.contains(localKey)) {
                        outLocalFileToDelete.call(localItem);
                    }
                    continue;
                } else {
                    onRemoteRemovedItemNotActual.call(localKey, remoteRemovedItem);
                }
            }

            if (remoteItem == null) {
                //remote not exists

                //upload to remote
                onRemoteItemAdded.call(localKey, localItem);
                if (!remoteExistingFiles.contains(localKey)) {
                    outLocalFileToUpload.call(localItem);
                }
            } else {
                //remote exists

                //process change
                if (!itemPriorityFunction.call(localItem, remoteItem)
                        && changeInspector.call(localItem, remoteItem)) {
                    //change local file
                    onLocalItemChanged.call(localKey, localItem, remoteItem);
                    outRemoteFileToDownload.call(remoteItem, false);
                }

                //local file not exists, download
                if (!localExistingFiles.contains(localKey)) {
                    outRemoteFileToDownload.call(localItem, false);
                }
            }
        }

        for (Map.Entry<K, T> entry : remoteItems.entrySet()) {
            K remoteKey = entry.getKey();
            T remoteItem = entry.getValue();
            T localItem = localItems.get(remoteKey);

            //delete remote
            R localRemovedItem = localRemovedItems.get(remoteKey);
            if (localRemovedItem != null) {
                if (removedItemPriorityFunction.call(remoteItem, localRemovedItem)) {
                    omRemoteItemRemoved.call(remoteKey, remoteItem);
                    if (remoteExistingFiles.contains(remoteKey)) {
                        outRemoteFileToDelete.call(remoteItem);
                    }
                    continue;
                } else {
                    onLocalRemovedItemNotActual.call(remoteKey, localRemovedItem);
                }
            }

            if (localItem == null) {
                //process changes?
                onLocalItemAdded.call(remoteKey, remoteItem);
                outRemoteFileToDownload.call(remoteItem, false);
            } else {
                //local exists

                //process change
                if (itemPriorityFunction.call(localItem, remoteItem) &&
                        changeInspector.call(localItem, remoteItem)) {
                    //change remote file
                    onRemoteItemChanged.call(remoteKey, remoteItem, localItem);
                    outLocalFileToUpload.call(localItem);
                }

                //remote file not exists, upload
                if (!remoteExistingFiles.contains(remoteKey)) {
                    outLocalFileToUpload.call(remoteItem);
                }
            }
        }

        for (K remoteExistingFileKey: remoteExistingFiles) {
            //file was uploaded to cloud case
            if (!localItems.containsKey(remoteExistingFileKey)
                    && !remoteItems.containsKey(remoteExistingFileKey)
                    && !localRemovedItems.containsKey(remoteExistingFileKey)
                    && !remoteRemovedItems.containsKey(remoteExistingFileKey)) {
                T item = itemDataCreator.map(remoteExistingFileKey);
                onLocalItemAdded.call(remoteExistingFileKey, item);
                onRemoteItemAdded.call(remoteExistingFileKey, item);
                outRemoteFileToDownload.call(item, true);
            }
        }
    }

    //actual item callback?
    public static <K, T> void mergeMaps(
            Map<K, T> localItems,
            Map<K, T> remoteItems,
            Mapper<T, Boolean> isItemActual,
            BiCallback<K, T> onLocalItemAdded,
            BiCallback<K, T> onLocalItemRemoved,
            BiCallback<K, T> onRemoteItemAdded,
            BiCallback<K, T> omRemoteItemRemoved) {

        for (Map.Entry<K, T> entry: localItems.entrySet()) {
            K localKey = entry.getKey();
            T localItem = entry.getValue();
            T remoteItem = remoteItems.get(localKey);

            if (!isItemActual.map(localItem)) {
                onLocalItemRemoved.call(localKey, localItem);
                continue;
            }

            if (remoteItem == null) {
                //remote not exists
                onRemoteItemAdded.call(localKey, localItem);
            }
        }

        for (Map.Entry<K, T> entry : remoteItems.entrySet()) {
            K remoteKey = entry.getKey();
            T remoteItem = entry.getValue();
            T localItem = localItems.get(remoteKey);

            if (!isItemActual.map(remoteItem)) {
                omRemoteItemRemoved.call(remoteKey, remoteItem);
                continue;
            }

            if (localItem == null) {
                onLocalItemAdded.call(remoteKey, remoteItem);
            }
        }
    }

}
