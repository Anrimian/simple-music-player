package com.github.anrimian.musicplayer.domain.models.albums;

public class Album {

    private final long id;
    private final String name;
    private final String artist;
    private final int compositionsCount;

    public Album(long id, String name, String artist, int compositionsCount) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.compositionsCount = compositionsCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        return id == album.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", compositionsCount=" + compositionsCount +
                '}';
    }
}
