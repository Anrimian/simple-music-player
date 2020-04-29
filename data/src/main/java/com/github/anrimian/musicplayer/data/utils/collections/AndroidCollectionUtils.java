package com.github.anrimian.musicplayer.data.utils.collections;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.domain.utils.functions.Mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AndroidCollectionUtils {

    public static <E> LongSparseArray<E> sparseArrayOf(long key, E item) {
        LongSparseArray<E> sparseArray = new LongSparseArray<>();
        sparseArray.put(key, item);
        return sparseArray;
    }

    public static <E> LongSparseArray<E> mapToSparseArray(List<E> from,
                                                          ListUtils.MapperFunction<E, Long> keySelector) {
        LongSparseArray<E> to = new LongSparseArray<>();
        for (E t: from) {
            Long key = keySelector.map(t);
            if (key != null) {
                to.put(key, t);
            }
        }
        return to;
    }


    public static <V> boolean processChanges(LongSparseArray<V> oldMap,
                                             LongSparseArray<V> newMap,
                                             ChangeInspector<V, V> changeInspector,
                                             Callback<V> onDeleteCallback,
                                             Callback<V> onAddedCallback,
                                             Callback<V> onModifyCallback) {
        boolean hasChanges = false;

        for(int i = 0, size = oldMap.size(); i < size; i++) {
            V existValue = oldMap.valueAt(i);
            long existKey = oldMap.keyAt(i);
            V value = newMap.get(existKey);
            if (value == null) {
                onDeleteCallback.call(existValue);
                hasChanges = true;
            }
        }

        for(int i = 0, size = newMap.size(); i < size; i++) {
            long newKey = newMap.keyAt(i);
            V newValue = newMap.valueAt(i);

            V existValue = oldMap.get(newKey);
            if (existValue == null) {
                onAddedCallback.call(newValue);
                hasChanges = true;
            } else if (changeInspector.hasChanges(newValue, existValue)) {
                onModifyCallback.call(newValue);
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    public static <V1, V2> boolean processDiffChanges(LongSparseArray<V1> oldMap,
                                                      LongSparseArray<V2> newMap,
                                                      ChangeInspector<V1, V2> changeInspector,
                                                      Callback<V1> onDeleteCallback,
                                                      Callback<V2> onAddedCallback,
                                                      BiCallback<V1, V2> onModifyCallback) {
        boolean hasChanges = false;

        for(int i = 0, size = oldMap.size(); i < size; i++) {
            V1 existValue = oldMap.valueAt(i);
            long existKey = oldMap.keyAt(i);
            V2 value = newMap.get(existKey);
            if (value == null) {
                onDeleteCallback.call(existValue);
                hasChanges = true;
            }
        }

        for(int i = 0, size = newMap.size(); i < size; i++) {
            long newKey = newMap.keyAt(i);
            V2 newValue = newMap.valueAt(i);

            V1 existValue = oldMap.get(newKey);
            if (existValue == null) {
                onAddedCallback.call(newValue);
                hasChanges = true;
            } else if (changeInspector.hasChanges(existValue, newValue)) {
                onModifyCallback.call(existValue, newValue);
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    public static <Old, New, NewKey> boolean processChanges(Set<Old> currentSet,
                                                            Map<NewKey, New> newMap,
                                                            Mapper<Old, NewKey> newKeyFetcher,
                                                            Mapper<New, Old> oldValueFetcher,
                                                            EqualComparator<Old, New> equalComparator,
                                                            Callback<Old> onDeleteCallback,
                                                            Callback<New> onAddedCallback,
                                                            BiCallback<Old, New> onModifyCallback) {
        boolean hasChanges = false;

        for(Old existValue: currentSet ) {

            NewKey existKey = newKeyFetcher.map(existValue);
            New newValue = newMap.get(existKey);

            if (newValue == null) {
                onDeleteCallback.call(existValue);
                hasChanges = true;
            }
        }

        for (Map.Entry<NewKey, New> newEntry: newMap.entrySet()) {
            New newValue = newEntry.getValue();

            Old existValue = oldValueFetcher.map(newValue);
            if (!currentSet.contains(existValue)) {
                onAddedCallback.call(newValue);
                hasChanges = true;
            } else if (equalComparator.areItemsTheSame(existValue, newValue)) {
                onModifyCallback.call(existValue, newValue);
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    public interface ChangeInspector<V1, V2> {
        boolean hasChanges(V1 first, V2 second);
    }

    public interface EqualComparator<T, K> {
        boolean areItemsTheSame(T first, K second);
    }
}
