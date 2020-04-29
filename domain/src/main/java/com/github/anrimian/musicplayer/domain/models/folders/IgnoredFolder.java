package com.github.anrimian.musicplayer.domain.models.folders;

import java.util.Date;

public class IgnoredFolder {

    private String relativePath;
    private Date addDate;

    public IgnoredFolder(String relativePath, Date addDate) {
        this.relativePath = relativePath;
        this.addDate = addDate;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public Date getAddDate() {
        return addDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IgnoredFolder that = (IgnoredFolder) o;

        return relativePath.equals(that.relativePath);
    }

    @Override
    public int hashCode() {
        return relativePath.hashCode();
    }
}
