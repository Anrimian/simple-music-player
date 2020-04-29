package com.github.anrimian.musicplayer.ui.common.images.glide.album;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public final class AlbumCoverLoader implements ModelLoader<Album, Bitmap> {

    private final Context context;

    AlbumCoverLoader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LoadData<Bitmap> buildLoadData(Album model,
                                          int width,
                                          int height,
                                          @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model.getId()), new AlbumCoverFetcher(model, context));
    }

    @Override
    public boolean handles(@NonNull Album model) {
        return true;
    }

}