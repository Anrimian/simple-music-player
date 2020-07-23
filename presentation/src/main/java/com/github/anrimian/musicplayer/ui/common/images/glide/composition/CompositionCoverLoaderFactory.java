package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;

import javax.annotation.Nonnull;

public class CompositionCoverLoaderFactory implements ModelLoaderFactory<CompositionImage, Bitmap> {

    private final Context context;
    private final CompositionSourceProvider compositionSourceProvider;

    public CompositionCoverLoaderFactory(Context context, CompositionSourceProvider compositionSourceProvider) {
        this.context = context;
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @Nonnull
    @Override
    public ModelLoader<CompositionImage, Bitmap> build(@Nonnull MultiModelLoaderFactory unused) {
        return new CompositionCoverLoader(context, compositionSourceProvider);
    }

    @Override
    public void teardown() {
        // Do nothing.
    }

}