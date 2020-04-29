package com.github.anrimian.musicplayer.domain.models.genres;

public class Genre {
    private final long id;

    private final String name;

    private final int compositionsCount;
    private final long totalDuration;

    public Genre(long id, String name, int compositionsCount, long totalDuration) {
        this.id = id;
        this.name = name;
        this.compositionsCount = compositionsCount;
        this.totalDuration = totalDuration;
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

    public long getTotalDuration() {
        return totalDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;

        return id == genre.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", compositionsCount=" + compositionsCount +
                ", totalDuration=" + totalDuration +
                '}';
    }
}
