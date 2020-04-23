package com.github.anrimian.musicplayer.domain.interactors.sync;


import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.functions.BiFunction;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;
import com.github.anrimian.musicplayer.domain.utils.functions.TripleCallback;

import java.util.Map;
import java.util.Set;


public class FileStructMerger {

    //move change(+ move command list)
    public static <K, T> void mergeFilesMap(
            Map<K, T> localItems,
            Map<K, T> remoteItems,
            Set<K> localRemovedItems,
            Set<K> remoteRemovedItems,
            Set<K> localExistingFiles,
            Set<K> remoteExistingFiles,
            BiFunction<T, T, Boolean> changeInspector,//we really need it?
            BiFunction<T, T, Boolean> itemPriorityFunction,
            Mapper<K, T> itemDataCreator,
            Callback<T> outLocalFileToDelete,
            Callback<T> outRemoteFileToDelete,
            Callback<T> outLocalFileToUpload,
            Callback<T> outRemoteFileToDownload,
            BiCallback<K, T> onLocalItemAdded,
            BiCallback<K, T> onLocalItemRemoved,
            TripleCallback<K, T, T> onLocalItemChanged,
            BiCallback<K, T> onRemoteItemAdded,
            BiCallback<K, T> omRemoteItemRemoved,
            TripleCallback<K, T, T> onRemoteItemChanged) {

        for (Map.Entry<K, T> entry: localItems.entrySet()) {
            K localKey = entry.getKey();
            T localItem = entry.getValue();
            T remoteItem = remoteItems.get(localKey);

            //delete local
            if (remoteRemovedItems.contains(localKey)) {
                onLocalItemRemoved.call(localKey, localItem);
                outLocalFileToDelete.call(localItem);
                continue;
            }

            if (remoteItem == null) {
                //remote not exists

                //upload to remote
                onRemoteItemAdded.call(localKey, localItem);
                outLocalFileToUpload.call(localItem);
            } else {
                //remote exists

                //process change
                if (!itemPriorityFunction.call(localItem, remoteItem)
                        && changeInspector.call(localItem, remoteItem)) {
                    //change local file
                    onLocalItemChanged.call(localKey, localItem, remoteItem);
                    outRemoteFileToDownload.call(remoteItem);
                }

                //local file not exists, download
                if (!localExistingFiles.contains(localKey)) {
                    outRemoteFileToDownload.call(localItem);
                }
            }
        }

        for (Map.Entry<K, T> entry : remoteItems.entrySet()) {
            K remoteKey = entry.getKey();
            T remoteItem = entry.getValue();
            T localItem = localItems.get(remoteKey);

            //delete remote
            if (localRemovedItems.contains(remoteKey)) {
                omRemoteItemRemoved.call(remoteKey, remoteItem);
                outRemoteFileToDelete.call(remoteItem);
                continue;
            }

            if (localItem == null) {
                //process changes?
                onLocalItemAdded.call(remoteKey, remoteItem);
                outRemoteFileToDownload.call(remoteItem);
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
                    && !localRemovedItems.contains(remoteExistingFileKey)
                    && !remoteRemovedItems.contains(remoteExistingFileKey)) {
                T item = itemDataCreator.map(remoteExistingFileKey);
                onLocalItemAdded.call(remoteExistingFileKey, item);
                onRemoteItemAdded.call(remoteExistingFileKey, item);
                outRemoteFileToDownload.call(item);
            }
        }
    }
}
