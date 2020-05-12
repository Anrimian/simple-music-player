package com.github.anrimian.musicplayer.domain.utils.changes;

public class Change<T> {
    private final T oldData;
    private final T newData;

    public Change(T oldData, T newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    public T getOldData() {
        return oldData;
    }

    public T getNewData() {
        return newData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Change<?> change = (Change<?>) o;

        if (oldData != null ? !oldData.equals(change.oldData) : change.oldData != null)
            return false;
        return newData != null ? newData.equals(change.newData) : change.newData == null;
    }

    @Override
    public int hashCode() {
        int result = oldData != null ? oldData.hashCode() : 0;
        result = 31 * result + (newData != null ? newData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Change{" +
                "oldData=" + oldData +
                ", newData=" + newData +
                '}';
    }
}
