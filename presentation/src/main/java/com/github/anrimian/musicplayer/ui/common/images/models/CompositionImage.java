package com.github.anrimian.musicplayer.ui.common.images.models;

import java.util.Date;

public class CompositionImage {

    private final long id;
    private final Date lastModifyTime;

    public CompositionImage(long id, Date lastModifyTime) {
        this.id = id;
        this.lastModifyTime = lastModifyTime;
    }

    public long getId() {
        return id;
    }

    public Date getLastModifyTime() {
        return lastModifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionImage that = (CompositionImage) o;

        if (id != that.id) return false;
        return lastModifyTime != null ? lastModifyTime.equals(that.lastModifyTime) : that.lastModifyTime == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (lastModifyTime != null ? lastModifyTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompositionImage{" +
                "id=" + id +
                ", lastModifyTime=" + lastModifyTime +
                '}';
    }
}
