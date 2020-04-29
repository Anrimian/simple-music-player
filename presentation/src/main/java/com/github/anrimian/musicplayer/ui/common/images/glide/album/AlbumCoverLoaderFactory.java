package com.github.anrimian.musicplayer.ui.common.images.glide.album;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;

public class AlbumCoverLoaderFactory implements ModelLoaderFactory<Album, Bitmap> {

    private final Context context;

    public AlbumCoverLoaderFactory(Context context) {
        this.context = context;
    }

    @Nonnull
    @Override
    public ModelLoader<Album, Bitmap> build(@Nonnull MultiModelLoaderFactory unused) {
        return new AlbumCoverLoader(context);
    }

    @Override
    public void teardown() {
        // Do nothing.
    }

}