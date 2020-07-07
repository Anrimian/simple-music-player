package com.github.anrimian.musicplayer.data.storage.source;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.models.image.UriImageSource;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileSourceProvider {

    private Context context;

    public FileSourceProvider(Context context) {
        this.context = context;
    }

    public InputStream getImageStream(ImageSource imageSource) throws FileNotFoundException {
        if (imageSource instanceof UriImageSource) {
            Uri uri = ((UriImageSource) imageSource).getUri();
            return context.getContentResolver().openInputStream(uri);
        }
        throw new IllegalArgumentException("unknown image source: " + imageSource);
    }
}
