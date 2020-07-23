package com.github.anrimian.musicplayer.ui.common.images.glide.external;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

import javax.annotation.Nonnull;

public class UriCoverLoaderFactory implements ModelLoaderFactory<UriCompositionImage, Bitmap> {

    private final Context context;

    public UriCoverLoaderFactory(Context context) {
        this.context = context;
    }

    @Nonnull
    @Override
    public ModelLoader<UriCompositionImage, Bitmap> build(@Nonnull MultiModelLoaderFactory unused) {
        return new UriCoverLoader(context);
    }

    @Override
    public void teardown() {
        // Do nothing.
    }

}