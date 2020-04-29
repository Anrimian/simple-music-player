package com.github.anrimian.musicplayer.data.models.changes;

import androidx.annotation.NonNull;

public class Change<O, N> {
    private final O old;
    private final N obj;

    public Change(O old, N obj) {
        this.old = old;
        this.obj = obj;
    }

    public O getOld() {
        return old;
    }

    public N getObj() {
        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change<?, ?> change = (Change<?, ?>) o;

        if (!old.equals(change.old)) return false;
        return obj.equals(change.obj);
    }

    @Override
    public int hashCode() {
        int result = old.hashCode();
        result = 31 * result + obj.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Change{" +
                "old=" + old +
                ", obj=" + obj +
                '}';
    }
}
