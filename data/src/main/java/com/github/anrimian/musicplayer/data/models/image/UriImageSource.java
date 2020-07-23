package com.github.anrimian.musicplayer.data.models.image;

import android.net.Uri;

import com.github.anrimian.musicplayer.domain.models.image.ImageSource;

public class UriImageSource implements ImageSource {

    private final Uri uri;

    public UriImageSource(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
}
