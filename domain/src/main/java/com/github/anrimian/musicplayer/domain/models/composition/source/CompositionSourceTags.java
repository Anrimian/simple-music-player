package com.github.anrimian.musicplayer.domain.models.composition.source;

public class CompositionSourceTags {
    private final String title;
    private final String artist;
    private final String album;
    private final String albumArtist;

    public CompositionSourceTags(String title, String artist, String album, String albumArtist) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumArtist = albumArtist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }
}
