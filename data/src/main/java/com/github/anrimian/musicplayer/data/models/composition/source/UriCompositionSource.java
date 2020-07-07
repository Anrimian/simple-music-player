package com.github.anrimian.musicplayer.data.models.composition.source;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;

public class UriCompositionSource implements CompositionSource {

    private final Uri uri;
    private final String displayName;
    private final String title;
    private final String artist;
    private final String album;
    private final long duration;
    private final long size;
    private final byte[] imageBytes;

    public UriCompositionSource(Uri uri,
                                String displayName,
                                String title,
                                String artist,
                                String album,
                                long duration,
                                long size,
                                byte[] imageBytes) {
        this.uri = uri;
        this.displayName = displayName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.imageBytes = imageBytes;
    }

    public Uri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
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

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriCompositionSource source = (UriCompositionSource) o;

        return uri.equals(source.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "UriCompositionSource{" +
                "uri=" + uri +
                ", displayName='" + displayName + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
