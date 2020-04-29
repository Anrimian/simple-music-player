package com.github.anrimian.musicplayer.domain.models.artist;

public class Artist {

    private final long id;
    private final String name;
    private final int compositionsCount;
    private final int albumsCount;

    public Artist(long id, String name, int compositionsCount, int albumsCount) {
        this.id = id;
        this.name = name;
        this.compositionsCount = compositionsCount;
        this.albumsCount = albumsCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    public int getAlbumsCount() {
        return albumsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;

        return id == artist.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
