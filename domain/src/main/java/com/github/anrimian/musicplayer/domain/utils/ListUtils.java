package com.github.anrimian.musicplayer.domain.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class ListUtils {

    public static <T> List<T> asList(T object) {
        List<T> list = new ArrayList<>();
        list.add(object);
        return list;
    }

    @SafeVarargs
    public static <T> List<T> asList(T... objects) {
        return new ArrayList<>(Arrays.asList(objects));
    }

    public static <T, E> List<E> mapList(List<T> from, List<E> to, MapperFunction<T, E> mapper) {
        for (T t: from) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <T, E> Set<E> mapToSet(List<T> from, MapperFunction<T, E> mapper) {
        return mapToSet(from, new HashSet<>(from.size()), mapper);
    }

    public static <T, E> Set<E> mapToSet(List<T> from, Set<E> to, MapperFunction<T, E> mapper) {
        for (T t: from) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <K, E, T> List<T> mapToList(Map<K, E> from, List<T> to, MapperFunction<E, T> mapper) {
        for (E t: from.values()) {
            to.add(mapper.map(t));
        }
        return to;
    }

    public static <K, E>  Map<K, E> mapToMap(List<E> from, Map<K, E> to, MapperFunction<E, K> keySelector) {
        for (E t: from) {
            K key = keySelector.map(t);
            if (key != null) {
                to.put(key, t);
            }
        }
        return to;
    }

    public static <K, E, T> List<T> mapToList(Map<K, E> from, MapperFunction<E, T> mapper) {
        return mapToList(from, new ArrayList<>(from.size()), mapper);
    }

    public static <T, E> List<E> mapList(List<T> from, MapperFunction<T, E> mapper) {
        return mapList(from, new ArrayList<>(from.size()), mapper);
    }

    public static <T, E> List<E> mapListNotNull(List<T> from, MapperFunction<T, E> mapper) {
        List<E> to = new ArrayList<>(from.size());
        for (T t: from) {
            E value = mapper.map(t);
            if (value != null) {
                to.add(value);
            }
        }
        return to;
    }

    public static <K, V> void update(Map<K, V> map, K key, V value) {
        if (map.containsKey(key)) {
            map.put(key, value);
        }
    }

    public static void safeSwap(List<?> list, int i, int j) {
        if (i >= list.size() || j >= list.size()) {
            return;
        }
        Collections.swap(list, i, j);
    }

    public static boolean isIndexInRange(List<?> list, int index) {
        return index >= 0 && index < list.size();
    }

    public interface MapperFunction<T, E> {

        E map(T t);
    }
}
