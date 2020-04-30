package com.github.anrimian.musicplayer.domain.interactors.sync.models;

public class FileKey {

    private final String name;
    private final String path;

    public FileKey(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileKey fileKey = (FileKey) o;

        if (!name.equals(fileKey.name)) return false;
        return path.equals(fileKey.path);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FileKey{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
