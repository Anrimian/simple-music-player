package com.github.anrimian.musicplayer.domain.models.folders;

public class FolderFileSource implements FileSource {

    private final long id;
    private final String name;
    private final int filesCount;

    public FolderFileSource(long id, String name, int filesCount) {
        this.id = id;
        this.name = name;
        this.filesCount = filesCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFilesCount() {
        return filesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderFileSource that = (FolderFileSource) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
